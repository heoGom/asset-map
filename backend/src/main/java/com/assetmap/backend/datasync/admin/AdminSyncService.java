package com.assetmap.backend.datasync.admin;
import com.assetmap.backend.datasync.common.DataSyncPolicyService;
import com.assetmap.backend.datasync.execution.SecurityMasterSyncService;
import com.assetmap.backend.datasync.execution.ExternalDataSyncCheckpointService;
import com.assetmap.backend.datasync.plan.SyncPlanService;
import com.assetmap.backend.datasync.status.DataSyncStatusService;
import com.assetmap.backend.datasync.plan.YearRange;
import com.assetmap.backend.datasync.execution.StockDividendCheckpointResult;
import com.assetmap.backend.datasync.execution.MarketPriceCheckpointResult;
import com.assetmap.backend.datasync.plan.StockDividendSyncPlan;
import com.assetmap.backend.datasync.plan.StockDividendYearTarget;
import com.assetmap.backend.datasync.plan.MarketPriceSyncPlan;
import com.assetmap.backend.datasync.plan.MarketPriceDateTarget;
import com.assetmap.backend.datasync.execution.SyncUpsertResult;
import com.assetmap.backend.datasync.common.DataSyncDecision;
import com.assetmap.backend.datasync.common.DataSyncType;
import com.assetmap.backend.datasync.common.DataSyncSource;
import com.assetmap.backend.datasync.admin.dto.DataSyncStatusResponse;
import com.assetmap.backend.datasync.admin.dto.AdminSyncResponse;
import com.assetmap.backend.datasync.admin.dto.AdminSyncRequest;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.datasync.provider.SecurityMasterProvider;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AdminSyncService {

	public static final String ALL = "ALL";
	public static final String TRADED_SECURITIES = "TRADED_SECURITIES";
	public static final String TRADED_STOCK_SECURITIES = "TRADED_STOCK_SECURITIES";

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final DataSyncStatusService dataSyncStatusService;
	private final SecurityMasterProvider securityMasterProvider;
	private final SecurityMasterSyncService securityMasterSyncService;
	private final ExternalDataSyncCheckpointService checkpointService;
	private final SyncPlanService syncPlanService;
	private final SecurityItemRepository securityItemRepository;
	private final DataSyncPolicyService dataSyncPolicyService;
	private final String defaultSecurityMasterBasDd;
	private final int marketPriceDefaultLookbackDays;
	private final int marketPriceMaxBackfillDays;

	public AdminSyncService(
			DataSyncStatusService dataSyncStatusService,
			SecurityMasterProvider securityMasterProvider,
			SecurityMasterSyncService securityMasterSyncService,
			ExternalDataSyncCheckpointService checkpointService,
			SyncPlanService syncPlanService,
			SecurityItemRepository securityItemRepository,
			DataSyncPolicyService dataSyncPolicyService,
			@Value("${external.krx.security-master.default-bas-dd:}") String defaultSecurityMasterBasDd,
			@Value("${app.sync.market-prices.default-lookback-days:30}") int marketPriceDefaultLookbackDays,
			@Value("${app.sync.market-prices.max-backfill-days:60}") int marketPriceMaxBackfillDays
	) {
		this.dataSyncStatusService = dataSyncStatusService;
		this.securityMasterProvider = securityMasterProvider;
		this.securityMasterSyncService = securityMasterSyncService;
		this.checkpointService = checkpointService;
		this.syncPlanService = syncPlanService;
		this.securityItemRepository = securityItemRepository;
		this.dataSyncPolicyService = dataSyncPolicyService;
		this.defaultSecurityMasterBasDd = defaultSecurityMasterBasDd;
		this.marketPriceDefaultLookbackDays = marketPriceDefaultLookbackDays;
		this.marketPriceMaxBackfillDays = marketPriceMaxBackfillDays;
	}

	public List<DataSyncStatusResponse> getStatuses() {
		return dataSyncStatusService.findAll();
	}

	@Transactional
	public AdminSyncResponse syncSecurityMaster(AdminSyncRequest request) {
		boolean force = force(request);
		LocalDate basDd = resolveSecurityMasterBasDd(request);
		String compactBasDd = basDd.format(COMPACT_DATE);
		DataSyncDecision decision = dataSyncPolicyService.decideDailySync(
				DataSyncType.SECURITY_MASTER,
				DataSyncSource.KRX,
				ALL,
				force,
				securityItemRepository.count() > 0
		);
		if (!decision.shouldRun()) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, decision.message());
			return response("SKIPPED", DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, compactBasDd, 0, 0, 0, 0, SyncUpsertResult.empty(), 0, decision.message(), status);
		}

		dataSyncStatusService.markRunning(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, "KRX security master sync started. basDd=" + compactBasDd);
		try {
			List<ImportedSecurityMaster> kospi = securityMasterProvider.fetchAllKospi(basDd);
			List<ImportedSecurityMaster> kosdaq = securityMasterProvider.fetchAllKosdaq(basDd);
			List<ImportedSecurityMaster> imported = new ArrayList<>(kospi);
			imported.addAll(kosdaq);
			SyncUpsertResult result = securityMasterSyncService.upsertImportedSecurities(imported);
			String message = "KRX security master sync completed. basDd=%s kospi=%d kosdaq=%d inserted=%d updated=%d skipped=%d"
					.formatted(compactBasDd, kospi.size(), kosdaq.size(), result.insertedCount(), result.updatedCount(), result.skippedCount());
			DataSyncStatusResponse status = dataSyncStatusService.markSuccess(
					DataSyncType.SECURITY_MASTER,
					DataSyncSource.KRX,
					ALL,
					LocalDate.now(),
					message
			);
			return response("SUCCESS", DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, compactBasDd, kospi.size(), kosdaq.size(), 0, 0, result, 0, message, status);
		} catch (BusinessException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, exception.getMessage());
			return response(exception.getErrorCode().name(), DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, compactBasDd, 0, 0, 0, 0, SyncUpsertResult.empty(), 1, exception.getMessage(), status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, exception.getMessage());
			return response("FAILED", DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, compactBasDd, 0, 0, 0, 0, SyncUpsertResult.empty(), 1, exception.getMessage(), status);
		}
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public AdminSyncResponse syncMarketPrices(AdminSyncRequest request) {
		boolean force = force(request);
		int configuredChunkDays = marketPriceMaxBackfillDays > 0 ? marketPriceMaxBackfillDays : marketPriceDefaultLookbackDays;
		int maxDatesPerRun = Math.max(1, configuredChunkDays);
		MarketPriceSyncPlan plan = syncPlanService.planMarketPrices(request, force, maxDatesPerRun);
		List<SecurityItem> targetSecurities = plan.targetSecurities();
		if (targetSecurities.isEmpty()) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, "No traded STOCK/ETF securities.");
			return response("SKIPPED", DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, "", 0, 0, 0, 0, SyncUpsertResult.empty(), 0, "No traded STOCK/ETF securities.", status);
		}

		List<MarketPriceDateTarget> dateTargets = plan.dateTargets();
		if (dateTargets.isEmpty()) {
			String message = "KRX market price sync skipped. Already up to date.";
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, message);
			return response("SKIPPED", DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, "", 0, 0, targetSecurities.size(), 0, SyncUpsertResult.empty(), 0, message, status);
		}
		LocalDate rangeStart = dateTargets.get(0).priceDate();
		LocalDate rangeEnd = dateTargets.get(dateTargets.size() - 1).priceDate();

		dataSyncStatusService.markRunning(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES,
				"KRX market price backfill started. from=%s to=%s target=%d chunkDates=%d".formatted(rangeStart, rangeEnd, targetSecurities.size(), dateTargets.size()));

		SyncUpsertResult total = SyncUpsertResult.empty();
		int importedPriceCount = 0;
		int failedCount = 0;
		LocalDate latestSuccessDate = null;
		for (MarketPriceDateTarget dateTarget : dateTargets) {
			MarketPriceCheckpointResult result = checkpointService.syncMarketPriceDate(dateTarget.priceDate(), dateTarget.targetSecurities());
			total = total.plus(result.upsertResult());
			importedPriceCount += result.importedPriceCount();
			failedCount += result.failedCount();
			if (result.success()) {
				latestSuccessDate = result.priceDate();
			}
		}

		String message = "KRX market price backfill completed. from=%s to=%s target=%d chunkDates=%d imported=%d skipped=%d failed=%d"
				.formatted(rangeStart, rangeEnd, targetSecurities.size(), dateTargets.size(), importedPriceCount, total.skippedCount(), failedCount);
		DataSyncStatusResponse status;
		String statusText;
		if (failedCount > 0 && importedPriceCount == 0) {
			status = dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, message);
			statusText = "FAILED";
		} else if (latestSuccessDate != null) {
			status = dataSyncStatusService.markSuccess(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, latestSuccessDate, message);
			statusText = "SUCCESS";
		} else {
			status = dataSyncStatusService.markSkipped(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, message);
			statusText = failedCount > 0 ? "FAILED" : "SKIPPED";
		}
		return response(statusText, DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES,
				rangeStart.format(COMPACT_DATE) + ".." + rangeEnd.format(COMPACT_DATE),
				0, 0, targetSecurities.size(), importedPriceCount, total, failedCount, message, status);
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public AdminSyncResponse syncStockDividends(AdminSyncRequest request) {
		boolean force = force(request);
		StockDividendSyncPlan plan = syncPlanService.planStockDividends(request, force);
		List<SecurityItem> targetSecurities = plan.targetSecurities();
		if (targetSecurities.isEmpty()) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, "No traded STOCK securities.");
			return response("SKIPPED", DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, "", 0, 0, 0, 0, SyncUpsertResult.empty(), 0, "No traded STOCK securities.", status);
		}

		YearRange range = plan.range();
		List<StockDividendYearTarget> yearTargets = plan.yearTargets();
		if (yearTargets.isEmpty()) {
			String message = "Stock dividend sync skipped. Already up to date or fresh NO_DATA checkpoints exist.";
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, message);
			return response("SKIPPED", DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES,
					range.fromYear() + ".." + range.toYear(), 0, 0, targetSecurities.size(), 0, SyncUpsertResult.empty(), 0, message, status);
		}
		dataSyncStatusService.markRunning(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES,
				"Stock dividend sync started. fromYear=%d toYear=%d target=%d checkpointTargets=%d".formatted(range.fromYear(), range.toYear(), targetSecurities.size(), yearTargets.size()));

		DividendImportResult total = DividendImportResult.empty();
		int failedCount = 0;
		int noDataCount = 0;
		for (StockDividendYearTarget yearTarget : yearTargets) {
			StockDividendCheckpointResult result = checkpointService.syncStockDividendSecurityYear(yearTarget.securityItem(), yearTarget.year());
			total = total.plus(result.importResult());
			failedCount += result.failedCount();
			if ("NO_DATA".equals(result.status())) {
				noDataCount++;
			}
		}

		String message = "Stock dividend sync completed. fromYear=%d toYear=%d target=%d checkpointTargets=%d imported=%d skipped=%d generatedPayments=%d noData=%d failed=%d"
				.formatted(range.fromYear(), range.toYear(), targetSecurities.size(), yearTargets.size(), total.importedEventCount(), total.skippedEventCount(), total.generatedPaymentCount(), noDataCount, failedCount);
		DataSyncStatusResponse status;
		String statusText;
		if (failedCount > 0 && total.importedEventCount() == 0 && noDataCount == 0) {
			status = dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, message);
			statusText = "FAILED";
		} else {
			status = dataSyncStatusService.markSuccess(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, LocalDate.now(), message);
			statusText = failedCount > 0 ? "PARTIAL_SUCCESS" : "SUCCESS";
		}
		return response(statusText, DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES,
				range.fromYear() + ".." + range.toYear(), 0, 0, targetSecurities.size(), 0,
				new SyncUpsertResult(total.targetSecurityCount(), total.importedEventCount(), 0, total.skippedEventCount()),
				failedCount, message, status);
	}

	private AdminSyncResponse response(
			String statusText,
			DataSyncType syncType,
			DataSyncSource source,
			String targetKey,
			String basDd,
			int kospiImportedCount,
			int kosdaqImportedCount,
			int targetSecurityCount,
			int importedPriceCount,
			SyncUpsertResult upsertResult,
			int failedCount,
			String message,
			DataSyncStatusResponse status
	) {
		return new AdminSyncResponse(
				statusText,
				syncType,
				source,
				targetKey,
				basDd,
				kospiImportedCount,
				kosdaqImportedCount,
				targetSecurityCount,
				importedPriceCount,
				upsertResult.receivedCount(),
				upsertResult.insertedCount(),
				upsertResult.updatedCount(),
				upsertResult.skippedCount(),
				failedCount,
				message,
				status
		);
	}

	private LocalDate resolveSecurityMasterBasDd(AdminSyncRequest request) {
		if (request != null && request.basDd() != null) {
			return request.basDd();
		}
		if (StringUtils.hasText(defaultSecurityMasterBasDd)) {
			return parseConfiguredDate(defaultSecurityMasterBasDd);
		}
		return LocalDate.now();
	}

	private LocalDate parseConfiguredDate(String value) {
		String trimmed = value.trim();
		try {
			if (trimmed.matches("\\d{8}")) {
				return LocalDate.parse(trimmed, COMPACT_DATE);
			}
			return LocalDate.parse(trimmed);
		} catch (DateTimeParseException exception) {
			return LocalDate.now();
		}
	}

	private boolean force(AdminSyncRequest request) {
		return request != null && request.forceOrFalse();
	}

}
