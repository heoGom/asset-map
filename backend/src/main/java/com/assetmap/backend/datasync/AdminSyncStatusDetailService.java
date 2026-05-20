package com.assetmap.backend.datasync;

import com.assetmap.backend.dividend.DataSourceType;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPriceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminSyncStatusDetailService {

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;
	private static final int RECENT_FAILURE_LIMIT = 10;

	private final DataSyncStatusService dataSyncStatusService;
	private final DataSyncStatusRepository dataSyncStatusRepository;
	private final SyncPlanService syncPlanService;
	private final MarketPriceRepository marketPriceRepository;
	private final DividendEventRepository dividendEventRepository;

	public AdminSyncStatusDetailService(
			DataSyncStatusService dataSyncStatusService,
			DataSyncStatusRepository dataSyncStatusRepository,
			SyncPlanService syncPlanService,
			MarketPriceRepository marketPriceRepository,
			DividendEventRepository dividendEventRepository
	) {
		this.dataSyncStatusService = dataSyncStatusService;
		this.dataSyncStatusRepository = dataSyncStatusRepository;
		this.syncPlanService = syncPlanService;
		this.marketPriceRepository = marketPriceRepository;
		this.dividendEventRepository = dividendEventRepository;
	}

	public AdminSyncStatusDetailResponse getDetail() {
		return new AdminSyncStatusDetailResponse(
				securityMasterStatus(),
				marketPriceStatus(),
				stockDividendStatus(),
				dataSyncStatusService.findAll()
		);
	}

	private AdminSyncStatusDetailResponse.SyncStatusSummary securityMasterStatus() {
		return dataSyncStatusRepository.findBySyncTypeAndSourceAndTargetKey(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, AdminSyncService.ALL)
				.map(status -> new AdminSyncStatusDetailResponse.SyncStatusSummary(
						status.getSyncType(),
						status.getSource(),
						status.getTargetKey(),
						status.getLastSuccessAt(),
						status.getLastFailureAt(),
						status.getStatus(),
						status.getMessage()
				))
				.orElse(new AdminSyncStatusDetailResponse.SyncStatusSummary(
						DataSyncType.SECURITY_MASTER,
						DataSyncSource.KRX,
						AdminSyncService.ALL,
						null,
						null,
						DataSyncStatusValue.SKIPPED,
						"No security master sync status."
				));
	}

	private AdminSyncStatusDetailResponse.MarketPriceSyncStatus marketPriceStatus() {
		MarketPriceSyncPlan plan = syncPlanService.planMarketPrices(new AdminSyncRequest(false, null, null, null, null), false, null);
		List<Long> targetIds = plan.targetSecurities().stream().map(security -> security.getId()).toList();
		int pricedSecurityCount = targetIds.isEmpty() ? 0 : Math.toIntExact(marketPriceRepository.countDistinctSecurityItemIdsWithPrice(MarketDataSource.KRX, targetIds));
		SyncNoDataCounts noDataCounts = syncPlanService.noDataCounts(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, SyncPlanService.TRADED_SECURITIES_PREFIX);
		return new AdminSyncStatusDetailResponse.MarketPriceSyncStatus(
				plan.targetSecurities().size(),
				pricedSecurityCount,
				Math.max(0, plan.targetSecurities().size() - pricedSecurityCount),
				plan.dateTargets().size(),
				noDataCounts.freshCount(),
				noDataCounts.expiredCount(),
				maxLastSuccessAt(DataSyncType.MARKET_PRICE, DataSyncSource.KRX),
				maxLastFailureAt(DataSyncType.MARKET_PRICE, DataSyncSource.KRX),
				recentFailedMarketPriceDates()
		);
	}

	private AdminSyncStatusDetailResponse.StockDividendSyncStatus stockDividendStatus() {
		StockDividendSyncPlan plan = syncPlanService.planStockDividends(new AdminSyncRequest(false, null, null, null, null), false);
		List<Long> targetIds = plan.targetSecurities().stream().map(security -> security.getId()).toList();
		int eventSecurityCount = targetIds.isEmpty() ? 0 : Math.toIntExact(dividendEventRepository.countDistinctSecurityItemIdsWithEvents(DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND, targetIds));
		SyncNoDataCounts noDataCounts = syncPlanService.noDataCounts(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, SyncPlanService.STOCK_DIVIDEND_PREFIX);
		return new AdminSyncStatusDetailResponse.StockDividendSyncStatus(
				plan.targetSecurities().size(),
				eventSecurityCount,
				plan.yearTargets().size(),
				noDataCounts.freshCount(),
				noDataCounts.expiredCount(),
				maxLastSuccessAt(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND),
				maxLastFailureAt(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND),
				recentFailedStockDividendSecurityYears()
		);
	}

	private List<AdminSyncStatusDetailResponse.MarketPriceFailedDate> recentFailedMarketPriceDates() {
		return dataSyncStatusRepository.findBySyncTypeAndSourceAndStatusAndTargetKeyStartingWithOrderByLastFailureAtDesc(
						DataSyncType.MARKET_PRICE,
						DataSyncSource.KRX,
						DataSyncStatusValue.FAILED,
						SyncPlanService.TRADED_SECURITIES_PREFIX
				)
				.stream()
				.limit(RECENT_FAILURE_LIMIT)
				.map(status -> new AdminSyncStatusDetailResponse.MarketPriceFailedDate(
						status.getTargetKey(),
						parseMarketPriceDate(status.getTargetKey()),
						status.getLastFailureAt(),
						status.getMessage()
				))
				.toList();
	}

	private List<AdminSyncStatusDetailResponse.StockDividendFailedSecurityYear> recentFailedStockDividendSecurityYears() {
		return dataSyncStatusRepository.findBySyncTypeAndSourceAndStatusAndTargetKeyStartingWithOrderByLastFailureAtDesc(
						DataSyncType.STOCK_DIVIDEND,
						DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND,
						DataSyncStatusValue.FAILED,
						SyncPlanService.STOCK_DIVIDEND_PREFIX
				)
				.stream()
				.limit(RECENT_FAILURE_LIMIT)
				.map(status -> {
					SecurityYear securityYear = parseStockDividendTargetKey(status.getTargetKey());
					return new AdminSyncStatusDetailResponse.StockDividendFailedSecurityYear(
							status.getTargetKey(),
							securityYear.securityItemId(),
							securityYear.year(),
							status.getLastFailureAt(),
							status.getMessage()
					);
				})
				.toList();
	}

	private LocalDateTime maxLastSuccessAt(DataSyncType syncType, DataSyncSource source) {
		return dataSyncStatusRepository.findBySyncTypeAndSource(syncType, source)
				.stream()
				.map(DataSyncStatus::getLastSuccessAt)
				.filter(time -> time != null)
				.max(LocalDateTime::compareTo)
				.orElse(null);
	}

	private LocalDateTime maxLastFailureAt(DataSyncType syncType, DataSyncSource source) {
		return dataSyncStatusRepository.findBySyncTypeAndSource(syncType, source)
				.stream()
				.map(DataSyncStatus::getLastFailureAt)
				.filter(time -> time != null)
				.max(LocalDateTime::compareTo)
				.orElse(null);
	}

	private LocalDate parseMarketPriceDate(String targetKey) {
		if (!targetKey.startsWith(SyncPlanService.TRADED_SECURITIES_PREFIX)) {
			return null;
		}
		try {
			return LocalDate.parse(targetKey.substring(SyncPlanService.TRADED_SECURITIES_PREFIX.length()), COMPACT_DATE);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}

	private SecurityYear parseStockDividendTargetKey(String targetKey) {
		if (!targetKey.startsWith(SyncPlanService.STOCK_DIVIDEND_PREFIX)) {
			return new SecurityYear(null, null);
		}
		String[] parts = targetKey.substring(SyncPlanService.STOCK_DIVIDEND_PREFIX.length()).split("_");
		if (parts.length != 2) {
			return new SecurityYear(null, null);
		}
		try {
			return new SecurityYear(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
		} catch (NumberFormatException exception) {
			return new SecurityYear(null, null);
		}
	}

	private record SecurityYear(Long securityItemId, Integer year) {
	}
}
