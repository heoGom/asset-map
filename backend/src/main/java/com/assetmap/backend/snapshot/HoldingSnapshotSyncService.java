package com.assetmap.backend.snapshot;

import com.assetmap.backend.datasync.admin.dto.AdminSyncRequest;
import com.assetmap.backend.datasync.admin.dto.AdminSyncResponse;
import com.assetmap.backend.datasync.admin.dto.DataSyncStatusResponse;
import com.assetmap.backend.datasync.common.DataSyncSource;
import com.assetmap.backend.datasync.common.DataSyncType;
import com.assetmap.backend.datasync.execution.SyncUpsertResult;
import com.assetmap.backend.datasync.status.DataSyncStatusRepository;
import com.assetmap.backend.datasync.status.DataSyncStatusService;
import com.assetmap.backend.datasync.status.enums.DataSyncStatusValue;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPriceRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HoldingSnapshotSyncService {

	public static final String ALL = "ALL";
	public static final String HOLDING_SNAPSHOT_PREFIX = "HOLDING_SNAPSHOT_";

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final MarketPriceRepository marketPriceRepository;
	private final HoldingSnapshotRepository snapshotRepository;
	private final DataSyncStatusRepository dataSyncStatusRepository;
	private final DataSyncStatusService dataSyncStatusService;
	private final HoldingSnapshotDateSyncService dateSyncService;
	private final int maxBackfillDays;
	private final int noDataRecheckDays;

	public HoldingSnapshotSyncService(
			MarketPriceRepository marketPriceRepository,
			HoldingSnapshotRepository snapshotRepository,
			DataSyncStatusRepository dataSyncStatusRepository,
			DataSyncStatusService dataSyncStatusService,
			HoldingSnapshotDateSyncService dateSyncService,
			@Value("${app.sync.holding-snapshots.max-backfill-days:30}") int maxBackfillDays,
			@Value("${app.sync.no-data-recheck-days:7}") int noDataRecheckDays
	) {
		this.marketPriceRepository = marketPriceRepository;
		this.snapshotRepository = snapshotRepository;
		this.dataSyncStatusRepository = dataSyncStatusRepository;
		this.dataSyncStatusService = dataSyncStatusService;
		this.dateSyncService = dateSyncService;
		this.maxBackfillDays = maxBackfillDays;
		this.noDataRecheckDays = noDataRecheckDays;
	}

	public HoldingSnapshotSyncPlan plan(AdminSyncRequest request) {
		boolean force = request != null && request.forceOrFalse();
		LocalDate to = resolveToDate(request);
		LocalDate from = resolveFromDate(request, to);
		if (from.isAfter(to)) {
			return new HoldingSnapshotSyncPlan(List.of());
		}
		int limit = resolveMaxDates(request);
		Set<LocalDate> existingSnapshotDates = force
				? Set.of()
				: new HashSet<>(snapshotRepository.findDistinctSnapshotDatesBetween(from, to));
		List<LocalDate> dates = marketPriceRepository.findDistinctPriceDatesBySourceBetween(MarketDataSource.KRX, from, to)
				.stream()
				.sorted(Comparator.reverseOrder())
				.filter(date -> shouldSyncDate(date, force, existingSnapshotDates))
				.limit(limit)
				.toList();
		return new HoldingSnapshotSyncPlan(dates);
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public AdminSyncResponse sync(AdminSyncRequest request) {
		HoldingSnapshotSyncPlan plan = plan(request);
		if (plan.snapshotDates().isEmpty()) {
			String message = "Holding snapshot sync skipped. Already up to date or no KRX market price dates.";
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.HOLDING_SNAPSHOT, DataSyncSource.INTERNAL, ALL, message);
			return response("SKIPPED", "", SyncUpsertResult.empty(), 0, 0, message, status);
		}

		dataSyncStatusService.markRunning(
				DataSyncType.HOLDING_SNAPSHOT,
				DataSyncSource.INTERNAL,
				ALL,
				"Holding snapshot sync started. dateCount=%d".formatted(plan.snapshotDates().size())
		);

		SyncUpsertResult total = SyncUpsertResult.empty();
		int targetPositionCount = 0;
		int failedCount = 0;
		LocalDate latestSuccessDate = null;
		for (LocalDate snapshotDate : plan.snapshotDates()) {
			HoldingSnapshotDateSyncResult result = dateSyncService.syncDate(snapshotDate);
			total = total.plus(new SyncUpsertResult(
					result.insertedCount() + result.updatedCount() + result.skippedCount(),
					result.insertedCount(),
					result.updatedCount(),
					result.skippedCount()
			));
			targetPositionCount += result.targetPositionCount();
			failedCount += result.failedCount();
			if (result.success()) {
				latestSuccessDate = snapshotDate;
			}
		}

		LocalDate rangeStart = plan.snapshotDates().stream().min(Comparator.naturalOrder()).orElse(null);
		LocalDate rangeEnd = plan.snapshotDates().stream().max(Comparator.naturalOrder()).orElse(null);
		String basDd = rangeStart == null ? "" : rangeStart.format(COMPACT_DATE) + ".." + rangeEnd.format(COMPACT_DATE);
		String message = "Holding snapshot sync completed. from=%s to=%s dateCount=%d targetPositions=%d inserted=%d updated=%d skipped=%d failed=%d"
				.formatted(rangeStart, rangeEnd, plan.snapshotDates().size(), targetPositionCount, total.insertedCount(), total.updatedCount(), total.skippedCount(), failedCount);
		DataSyncStatusResponse status;
		String statusText;
		if (failedCount > 0 && total.insertedCount() + total.updatedCount() == 0) {
			status = dataSyncStatusService.markFailed(DataSyncType.HOLDING_SNAPSHOT, DataSyncSource.INTERNAL, ALL, message);
			statusText = "FAILED";
		} else if (latestSuccessDate != null) {
			status = dataSyncStatusService.markSuccess(DataSyncType.HOLDING_SNAPSHOT, DataSyncSource.INTERNAL, ALL, latestSuccessDate, message);
			statusText = failedCount > 0 ? "PARTIAL_SUCCESS" : "SUCCESS";
		} else {
			status = dataSyncStatusService.markNoData(DataSyncType.HOLDING_SNAPSHOT, DataSyncSource.INTERNAL, ALL, rangeEnd == null ? LocalDate.now() : rangeEnd, message);
			statusText = "NO_DATA";
		}
		return response(statusText, basDd, total, targetPositionCount, failedCount, message, status);
	}

	private boolean shouldSyncDate(LocalDate date, boolean force, Set<LocalDate> existingSnapshotDates) {
		if (force) {
			return true;
		}
		String targetKey = HoldingSnapshotDateSyncService.targetKey(date);
		if (dataSyncStatusService.hasFreshNoDataSync(DataSyncType.HOLDING_SNAPSHOT, DataSyncSource.INTERNAL, targetKey, noDataRecheckDays)) {
			return false;
		}
		boolean shouldRetryStatus = dataSyncStatusRepository
				.findBySyncTypeAndSourceAndTargetKey(DataSyncType.HOLDING_SNAPSHOT, DataSyncSource.INTERNAL, targetKey)
				.map(status -> status.getStatus() == DataSyncStatusValue.FAILED || status.getStatus() == DataSyncStatusValue.RUNNING)
				.orElse(false);
		if (shouldRetryStatus) {
			return true;
		}
		return !existingSnapshotDates.contains(date);
	}

	private LocalDate resolveToDate(AdminSyncRequest request) {
		if (request != null && request.toDate() != null) {
			return request.toDate();
		}
		if (request != null && request.priceDate() != null) {
			return request.priceDate();
		}
		if (request != null && request.basDd() != null) {
			return request.basDd();
		}
		return LocalDate.now();
	}

	private LocalDate resolveFromDate(AdminSyncRequest request, LocalDate to) {
		if (request != null && request.fromDate() != null) {
			return request.fromDate();
		}
		if (request != null && request.priceDate() != null) {
			return request.priceDate();
		}
		return LocalDate.of(1900, 1, 1);
	}

	private int resolveMaxDates(AdminSyncRequest request) {
		if (request != null && request.maxDates() != null && request.maxDates() > 0) {
			return request.maxDates();
		}
		return Math.max(1, maxBackfillDays);
	}

	private AdminSyncResponse response(
			String statusText,
			String basDd,
			SyncUpsertResult upsertResult,
			int targetPositionCount,
			int failedCount,
			String message,
			DataSyncStatusResponse status
	) {
		return new AdminSyncResponse(
				statusText,
				DataSyncType.HOLDING_SNAPSHOT,
				DataSyncSource.INTERNAL,
				ALL,
				basDd,
				0,
				0,
				targetPositionCount,
				0,
				upsertResult.receivedCount(),
				upsertResult.insertedCount(),
				upsertResult.updatedCount(),
				upsertResult.skippedCount(),
				failedCount,
				message,
				status
		);
	}
}
