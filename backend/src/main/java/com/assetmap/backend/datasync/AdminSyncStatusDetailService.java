package com.assetmap.backend.datasync;

import com.assetmap.backend.dividend.DataSourceType;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPriceRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityType;
import com.assetmap.backend.transaction.SecurityTradeStartProjection;
import com.assetmap.backend.transaction.TradeTransactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AdminSyncStatusDetailService {

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;
	private static final String TRADED_SECURITIES_PREFIX = AdminSyncService.TRADED_SECURITIES + "_";
	private static final String STOCK_DIVIDEND_PREFIX = "STOCK_DIVIDEND_";
	private static final int RECENT_FAILURE_LIMIT = 10;

	private final DataSyncStatusService dataSyncStatusService;
	private final DataSyncStatusRepository dataSyncStatusRepository;
	private final TradeTransactionRepository tradeTransactionRepository;
	private final MarketPriceRepository marketPriceRepository;
	private final DividendEventRepository dividendEventRepository;
	private final int noDataRecheckDays;
	private final int stockDividendDefaultFromYear;
	private final int stockDividendRecheckYears;

	public AdminSyncStatusDetailService(
			DataSyncStatusService dataSyncStatusService,
			DataSyncStatusRepository dataSyncStatusRepository,
			TradeTransactionRepository tradeTransactionRepository,
			MarketPriceRepository marketPriceRepository,
			DividendEventRepository dividendEventRepository,
			@Value("${app.sync.no-data-recheck-days:7}") int noDataRecheckDays,
			@Value("${app.sync.stock-dividends.default-from-year:2020}") int stockDividendDefaultFromYear,
			@Value("${app.sync.stock-dividends.recheck-years:2}") int stockDividendRecheckYears
	) {
		this.dataSyncStatusService = dataSyncStatusService;
		this.dataSyncStatusRepository = dataSyncStatusRepository;
		this.tradeTransactionRepository = tradeTransactionRepository;
		this.marketPriceRepository = marketPriceRepository;
		this.dividendEventRepository = dividendEventRepository;
		this.noDataRecheckDays = noDataRecheckDays;
		this.stockDividendDefaultFromYear = stockDividendDefaultFromYear;
		this.stockDividendRecheckYears = stockDividendRecheckYears;
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
		List<SecurityItem> targets = tradeTransactionRepository.findDistinctSecurityItemsBySecurityTypes(List.of(SecurityType.STOCK, SecurityType.ETF));
		List<Long> targetIds = targets.stream().map(SecurityItem::getId).toList();
		int pricedSecurityCount = targetIds.isEmpty() ? 0 : Math.toIntExact(marketPriceRepository.countDistinctSecurityItemIdsWithPrice(MarketDataSource.KRX, targetIds));
		int pendingDateCount = countPendingMarketPriceDates(targets);
		NoDataCounts noDataCounts = noDataCounts(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES_PREFIX);
		return new AdminSyncStatusDetailResponse.MarketPriceSyncStatus(
				targets.size(),
				pricedSecurityCount,
				targets.size() - pricedSecurityCount,
				pendingDateCount,
				noDataCounts.freshCount(),
				noDataCounts.expiredCount(),
				maxLastSuccessAt(DataSyncType.MARKET_PRICE, DataSyncSource.KRX),
				maxLastFailureAt(DataSyncType.MARKET_PRICE, DataSyncSource.KRX),
				recentFailedMarketPriceDates()
		);
	}

	private AdminSyncStatusDetailResponse.StockDividendSyncStatus stockDividendStatus() {
		List<SecurityItem> targets = tradeTransactionRepository.findDistinctSecurityItemsBySecurityTypes(List.of(SecurityType.STOCK))
				.stream()
				.filter(this::isDomesticKrwStock)
				.toList();
		List<Long> targetIds = targets.stream().map(SecurityItem::getId).toList();
		int eventSecurityCount = targetIds.isEmpty() ? 0 : Math.toIntExact(dividendEventRepository.countDistinctSecurityItemIdsWithEvents(DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND, targetIds));
		int pendingSecurityYearCount = countPendingStockDividendSecurityYears(targets);
		NoDataCounts noDataCounts = noDataCounts(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, STOCK_DIVIDEND_PREFIX);
		return new AdminSyncStatusDetailResponse.StockDividendSyncStatus(
				targets.size(),
				eventSecurityCount,
				pendingSecurityYearCount,
				noDataCounts.freshCount(),
				noDataCounts.expiredCount(),
				maxLastSuccessAt(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND),
				maxLastFailureAt(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND),
				recentFailedStockDividendSecurityYears()
		);
	}

	private int countPendingMarketPriceDates(List<SecurityItem> targets) {
		Map<Long, LocalDate> firstTradeDateBySecurityId = firstTradeDateBySecurityId(List.of(SecurityType.STOCK, SecurityType.ETF));
		LocalDate today = LocalDate.now();
		LocalDate start = targets.stream()
				.map(SecurityItem::getId)
				.map(firstTradeDateBySecurityId::get)
				.filter(date -> date != null && !date.isAfter(today))
				.min(LocalDate::compareTo)
				.orElse(today.plusDays(1));
		if (start.isAfter(today)) {
			return 0;
		}
		int pendingCount = 0;
		for (LocalDate priceDate = start; !priceDate.isAfter(today); priceDate = priceDate.plusDays(1)) {
			LocalDate candidateDate = priceDate;
			List<SecurityItem> eligibleTargets = targets.stream()
					.filter(security -> {
						LocalDate firstTradeDate = firstTradeDateBySecurityId.get(security.getId());
						return firstTradeDate != null && !firstTradeDate.isAfter(candidateDate);
					})
					.toList();
			if (eligibleTargets.isEmpty()) {
				continue;
			}
			List<Long> eligibleIds = eligibleTargets.stream().map(SecurityItem::getId).toList();
			Set<Long> existingIds = new HashSet<>(marketPriceRepository.findSecurityItemIdsWithPrice(MarketDataSource.KRX, priceDate, eligibleIds));
			if (existingIds.containsAll(eligibleIds)) {
				continue;
			}
			String targetKey = TRADED_SECURITIES_PREFIX + priceDate.format(COMPACT_DATE);
			if (existingIds.isEmpty() && dataSyncStatusService.hasFreshNoDataSync(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, noDataRecheckDays)) {
				continue;
			}
			pendingCount++;
		}
		return pendingCount;
	}

	private int countPendingStockDividendSecurityYears(List<SecurityItem> targets) {
		Map<Long, LocalDate> firstTradeDateBySecurityId = firstTradeDateBySecurityId(List.of(SecurityType.STOCK));
		int currentYear = LocalDate.now().getYear();
		int recheckFromYear = Math.max(stockDividendDefaultFromYear, currentYear - Math.max(1, stockDividendRecheckYears) + 1);
		int pendingCount = 0;
		for (SecurityItem securityItem : targets) {
			LocalDate firstTradeDate = firstTradeDateBySecurityId.get(securityItem.getId());
			if (firstTradeDate == null) {
				continue;
			}
			int fromYear = Math.max(stockDividendDefaultFromYear, firstTradeDate.getYear());
			for (int year = fromYear; year <= currentYear; year++) {
				long eventCount = dividendEventRepository.countBySourceAndSecurityItemIdAndDividendYear(
						DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND,
						securityItem.getId(),
						year
				);
				if (eventCount > 0) {
					if (year >= recheckFromYear) {
						pendingCount++;
					}
					continue;
				}
				String targetKey = ExternalDataSyncCheckpointService.stockDividendTargetKey(securityItem, year);
				if (!dataSyncStatusService.hasFreshNoDataSync(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, targetKey, noDataRecheckDays)) {
					pendingCount++;
				}
			}
		}
		return pendingCount;
	}

	private List<AdminSyncStatusDetailResponse.MarketPriceFailedDate> recentFailedMarketPriceDates() {
		return dataSyncStatusRepository.findBySyncTypeAndSourceAndStatusAndTargetKeyStartingWithOrderByLastFailureAtDesc(
						DataSyncType.MARKET_PRICE,
						DataSyncSource.KRX,
						DataSyncStatusValue.FAILED,
						TRADED_SECURITIES_PREFIX
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
						STOCK_DIVIDEND_PREFIX
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

	private NoDataCounts noDataCounts(DataSyncType syncType, DataSyncSource source, String targetKeyPrefix) {
		int fresh = 0;
		int expired = 0;
		for (DataSyncStatus status : dataSyncStatusRepository.findBySyncTypeAndSourceAndStatusAndTargetKeyStartingWith(
				syncType,
				source,
				DataSyncStatusValue.NO_DATA,
				targetKeyPrefix
		)) {
			if (isFreshNoData(status)) {
				fresh++;
			} else {
				expired++;
			}
		}
		return new NoDataCounts(fresh, expired);
	}

	private boolean isFreshNoData(DataSyncStatus status) {
		if (status.getLastSuccessAt() == null || noDataRecheckDays <= 0) {
			return false;
		}
		return status.getLastSuccessAt().isAfter(LocalDateTime.now().minusDays(noDataRecheckDays));
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

	private Map<Long, LocalDate> firstTradeDateBySecurityId(List<SecurityType> securityTypes) {
		Map<Long, LocalDate> firstTradeDateBySecurityId = new HashMap<>();
		for (SecurityTradeStartProjection projection : tradeTransactionRepository.findFirstTradeDateBySecurityTypes(securityTypes)) {
			firstTradeDateBySecurityId.put(projection.getSecurityItemId(), projection.getFirstTradeDate());
		}
		return firstTradeDateBySecurityId;
	}

	private LocalDate parseMarketPriceDate(String targetKey) {
		if (!targetKey.startsWith(TRADED_SECURITIES_PREFIX)) {
			return null;
		}
		try {
			return LocalDate.parse(targetKey.substring(TRADED_SECURITIES_PREFIX.length()), COMPACT_DATE);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}

	private SecurityYear parseStockDividendTargetKey(String targetKey) {
		if (!targetKey.startsWith(STOCK_DIVIDEND_PREFIX)) {
			return new SecurityYear(null, null);
		}
		String[] parts = targetKey.substring(STOCK_DIVIDEND_PREFIX.length()).split("_");
		if (parts.length != 2) {
			return new SecurityYear(null, null);
		}
		try {
			return new SecurityYear(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
		} catch (NumberFormatException exception) {
			return new SecurityYear(null, null);
		}
	}

	private boolean isDomesticKrwStock(SecurityItem securityItem) {
		if (securityItem.getSecurityType() != SecurityType.STOCK) {
			return false;
		}
		if (!"KRW".equalsIgnoreCase(securityItem.getCurrency())) {
			return false;
		}
		if (!StringUtils.hasText(securityItem.getCountry())) {
			return true;
		}
		String country = securityItem.getCountry().replaceAll("[\\s\\-_/().]", "").toUpperCase(Locale.ROOT);
		return country.equals("KR") || country.equals("KOREA") || country.equals("SOUTHKOREA") || country.equals("대한민국");
	}

	private record NoDataCounts(int freshCount, int expiredCount) {
	}

	private record SecurityYear(Long securityItemId, Integer year) {
	}
}
