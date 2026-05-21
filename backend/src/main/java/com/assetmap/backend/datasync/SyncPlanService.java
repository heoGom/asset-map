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
import java.util.ArrayList;
import java.util.Comparator;
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
public class SyncPlanService {

	static final String TRADED_SECURITIES_PREFIX = AdminSyncService.TRADED_SECURITIES + "_";
	static final String STOCK_DIVIDEND_PREFIX = "STOCK_DIVIDEND_";

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final DataSyncStatusRepository dataSyncStatusRepository;
	private final DataSyncStatusService dataSyncStatusService;
	private final TradeTransactionRepository tradeTransactionRepository;
	private final MarketPriceRepository marketPriceRepository;
	private final DividendEventRepository dividendEventRepository;
	private final int noDataRecheckDays;
	private final int stockDividendDefaultFromYear;
	private final int stockDividendRecheckYears;

	public SyncPlanService(
			DataSyncStatusRepository dataSyncStatusRepository,
			DataSyncStatusService dataSyncStatusService,
			TradeTransactionRepository tradeTransactionRepository,
			MarketPriceRepository marketPriceRepository,
			DividendEventRepository dividendEventRepository,
			@Value("${app.sync.no-data-recheck-days:7}") int noDataRecheckDays,
			@Value("${app.sync.stock-dividends.default-from-year:2020}") int stockDividendDefaultFromYear,
			@Value("${app.sync.stock-dividends.recheck-years:2}") int stockDividendRecheckYears
	) {
		this.dataSyncStatusRepository = dataSyncStatusRepository;
		this.dataSyncStatusService = dataSyncStatusService;
		this.tradeTransactionRepository = tradeTransactionRepository;
		this.marketPriceRepository = marketPriceRepository;
		this.dividendEventRepository = dividendEventRepository;
		this.noDataRecheckDays = noDataRecheckDays;
		this.stockDividendDefaultFromYear = stockDividendDefaultFromYear;
		this.stockDividendRecheckYears = stockDividendRecheckYears;
	}

	public MarketPriceSyncPlan planMarketPrices(AdminSyncRequest request, boolean force, Integer maxDatesPerRun) {
		List<SecurityItem> targetSecurities = tradeTransactionRepository.findDistinctSecurityItemsBySecurityTypes(List.of(SecurityType.STOCK, SecurityType.ETF));
		List<MarketPriceDateTarget> pendingTargets = resolveMarketPriceDateTargets(request, targetSecurities, force, maxDatesPerRun);
		return new MarketPriceSyncPlan(targetSecurities, pendingTargets);
	}

	public StockDividendSyncPlan planStockDividends(AdminSyncRequest request, boolean force) {
		List<SecurityItem> targetSecurities = tradedDomesticStocks();
		YearRange range = resolveStockDividendRange(request, force, targetSecurities);
		List<StockDividendYearTarget> yearTargets = resolveStockDividendYearTargets(force, targetSecurities, range);
		return new StockDividendSyncPlan(targetSecurities, range, yearTargets);
	}

	public SyncNoDataCounts noDataCounts(DataSyncType syncType, DataSyncSource source, String targetKeyPrefix) {
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
		return new SyncNoDataCounts(fresh, expired);
	}

	private List<MarketPriceDateTarget> resolveMarketPriceDateTargets(
			AdminSyncRequest request,
			List<SecurityItem> targetSecurities,
			boolean force,
			Integer maxDatesPerRun
	) {
		LocalDate explicitDate = resolveExplicitMarketPriceDate(request);
		Map<Long, LocalDate> firstTradeDateBySecurityId = firstTradeDateBySecurityId(List.of(SecurityType.STOCK, SecurityType.ETF));
		if (explicitDate != null) {
			List<SecurityItem> missingTargets = missingMarketPriceTargets(explicitDate, targetSecurities, firstTradeDateBySecurityId, force);
			return missingTargets.isEmpty() ? List.of() : List.of(new MarketPriceDateTarget(explicitDate, missingTargets));
		}
		LocalDate end = request != null && request.basDd() != null ? request.basDd() : LocalDate.now();
		LocalDate syncEnd = end;
		LocalDate start = targetSecurities.stream()
				.map(SecurityItem::getId)
				.map(firstTradeDateBySecurityId::get)
				.filter(date -> date != null && !date.isAfter(syncEnd))
				.min(Comparator.naturalOrder())
				.orElse(syncEnd.plusDays(1));
		if (start.isAfter(syncEnd)) {
			return List.of();
		}
		List<MarketPriceDateTarget> dateTargets = new ArrayList<>();
		for (LocalDate priceDate = syncEnd; !priceDate.isBefore(start); priceDate = priceDate.minusDays(1)) {
			List<SecurityItem> missingTargets = missingMarketPriceTargets(priceDate, targetSecurities, firstTradeDateBySecurityId, force);
			if (missingTargets.isEmpty()) {
				continue;
			}
			dateTargets.add(new MarketPriceDateTarget(priceDate, missingTargets));
			if (maxDatesPerRun != null && maxDatesPerRun > 0 && dateTargets.size() >= maxDatesPerRun) {
				break;
			}
		}
		return dateTargets.stream()
				.sorted(Comparator.comparing(MarketPriceDateTarget::priceDate))
				.toList();
	}

	private List<SecurityItem> missingMarketPriceTargets(
			LocalDate priceDate,
			List<SecurityItem> targetSecurities,
			Map<Long, LocalDate> firstTradeDateBySecurityId,
			boolean force
	) {
		List<SecurityItem> eligibleTargets = targetSecurities.stream()
				.filter(security -> {
					LocalDate firstTradeDate = firstTradeDateBySecurityId.get(security.getId());
					return firstTradeDate != null && !firstTradeDate.isAfter(priceDate);
				})
				.toList();
		if (eligibleTargets.isEmpty()) {
			return List.of();
		}
		if (force) {
			return eligibleTargets;
		}
		List<Long> eligibleIds = eligibleTargets.stream().map(SecurityItem::getId).toList();
		Set<Long> existingIds = new HashSet<>(marketPriceRepository.findSecurityItemIdsWithPrice(MarketDataSource.KRX, priceDate, eligibleIds));
		if (existingIds.containsAll(eligibleIds)) {
			return List.of();
		}
		String dateTargetKey = TRADED_SECURITIES_PREFIX + priceDate.format(COMPACT_DATE);
		if (existingIds.isEmpty() && dataSyncStatusService.hasFreshNoDataSync(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, dateTargetKey, noDataRecheckDays)) {
			return List.of();
		}
		return eligibleTargets.stream()
				.filter(security -> !existingIds.contains(security.getId()))
				.toList();
	}

	private YearRange resolveStockDividendRange(AdminSyncRequest request, boolean force, List<SecurityItem> targetSecurities) {
		int currentYear = LocalDate.now().getYear();
		if (request != null && request.fromYear() != null && request.toYear() != null) {
			return new YearRange(request.fromYear(), request.toYear());
		}
		if (force) {
			return new YearRange(request != null && request.fromYear() != null ? request.fromYear() : stockDividendDefaultFromYear,
					request != null && request.toYear() != null ? request.toYear() : currentYear);
		}
		Map<Long, LocalDate> firstTradeDateBySecurityId = firstTradeDateBySecurityId(List.of(SecurityType.STOCK));
		int earliestNeededYear = targetSecurities.stream()
				.map(SecurityItem::getId)
				.map(firstTradeDateBySecurityId::get)
				.filter(date -> date != null)
				.mapToInt(LocalDate::getYear)
				.min()
				.orElse(stockDividendDefaultFromYear);
		int fullFromYear = Math.max(stockDividendDefaultFromYear, earliestNeededYear);
		int recheckFromYear = Math.max(fullFromYear, LocalDate.now().getYear() - Math.max(1, stockDividendRecheckYears) + 1);
		int fromYear = hasHistoricalDividendGap(targetSecurities, firstTradeDateBySecurityId, fullFromYear, recheckFromYear - 1)
				? fullFromYear
				: recheckFromYear;
		return new YearRange(fromYear, currentYear);
	}

	private List<StockDividendYearTarget> resolveStockDividendYearTargets(
			boolean force,
			List<SecurityItem> targetSecurities,
			YearRange range
	) {
		Map<Long, LocalDate> firstTradeDateBySecurityId = firstTradeDateBySecurityId(List.of(SecurityType.STOCK));
		int currentYear = LocalDate.now().getYear();
		int recheckFromYear = Math.max(range.fromYear(), currentYear - Math.max(1, stockDividendRecheckYears) + 1);
		List<StockDividendYearTarget> yearTargets = new ArrayList<>();
		for (SecurityItem securityItem : targetSecurities) {
			LocalDate firstTradeDate = firstTradeDateBySecurityId.get(securityItem.getId());
			if (firstTradeDate == null) {
				continue;
			}
			int fromYear = Math.max(range.fromYear(), firstTradeDate.getYear());
			for (int year = fromYear; year <= range.toYear(); year++) {
				boolean recentRecheckYear = year >= recheckFromYear;
				if (shouldSyncStockDividendYear(securityItem, year, force, recentRecheckYear)) {
					yearTargets.add(new StockDividendYearTarget(securityItem, year));
				}
			}
		}
		return yearTargets;
	}

	private boolean shouldSyncStockDividendYear(SecurityItem securityItem, int year, boolean force, boolean recentRecheckYear) {
		if (force) {
			return true;
		}
		long eventCount = dividendEventRepository.countBySourceAndSecurityItemIdAndDividendYear(
				DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND,
				securityItem.getId(),
				year
		);
		if (eventCount > 0) {
			return recentRecheckYear;
		}
		String targetKey = ExternalDataSyncCheckpointService.stockDividendTargetKey(securityItem, year);
		return !dataSyncStatusService.hasFreshNoDataSync(
				DataSyncType.STOCK_DIVIDEND,
				DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND,
				targetKey,
				noDataRecheckDays
		);
	}

	private boolean hasHistoricalDividendGap(
			List<SecurityItem> targetSecurities,
			Map<Long, LocalDate> firstTradeDateBySecurityId,
			int defaultFromYear,
			int stableToYear
	) {
		if (stableToYear < defaultFromYear) {
			return false;
		}
		for (SecurityItem securityItem : targetSecurities) {
			LocalDate firstTradeDate = firstTradeDateBySecurityId.get(securityItem.getId());
			if (firstTradeDate == null) {
				continue;
			}
			int fromYear = Math.max(defaultFromYear, firstTradeDate.getYear());
			if (fromYear > stableToYear) {
				continue;
			}
			Set<Integer> yearsWithEvents = new HashSet<>();
			for (Object[] row : dividendEventRepository.countByDividendYear(
					DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND,
					securityItem.getId(),
					fromYear,
					stableToYear
			)) {
				if (row[0] instanceof Number year && row[1] instanceof Number count && count.longValue() > 0) {
					yearsWithEvents.add(year.intValue());
				}
			}
			for (int year = fromYear; year <= stableToYear; year++) {
				if (!yearsWithEvents.contains(year)) {
					return true;
				}
			}
		}
		return false;
	}

	private List<SecurityItem> tradedDomesticStocks() {
		return tradeTransactionRepository.findDistinctSecurityItemsBySecurityTypes(List.of(SecurityType.STOCK))
				.stream()
				.filter(this::isDomesticKrwStock)
				.toList();
	}

	private Map<Long, LocalDate> firstTradeDateBySecurityId(List<SecurityType> securityTypes) {
		Map<Long, LocalDate> firstTradeDateBySecurityId = new HashMap<>();
		for (SecurityTradeStartProjection projection : tradeTransactionRepository.findFirstTradeDateBySecurityTypes(securityTypes)) {
			firstTradeDateBySecurityId.put(projection.getSecurityItemId(), projection.getFirstTradeDate());
		}
		return firstTradeDateBySecurityId;
	}

	private LocalDate resolveExplicitMarketPriceDate(AdminSyncRequest request) {
		if (request == null) {
			return null;
		}
		if (request.priceDate() != null) {
			return request.priceDate();
		}
		return request.basDd();
	}

	private boolean isFreshNoData(DataSyncStatus status) {
		if (status.getLastSuccessAt() == null || noDataRecheckDays <= 0) {
			return false;
		}
		return status.getLastSuccessAt().isAfter(LocalDateTime.now().minusDays(noDataRecheckDays));
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
}

record MarketPriceSyncPlan(List<SecurityItem> targetSecurities, List<MarketPriceDateTarget> dateTargets) {
}

record MarketPriceDateTarget(LocalDate priceDate, List<SecurityItem> targetSecurities) {
}

record StockDividendSyncPlan(List<SecurityItem> targetSecurities, YearRange range, List<StockDividendYearTarget> yearTargets) {
}

record StockDividendYearTarget(SecurityItem securityItem, int year) {
}

record YearRange(int fromYear, int toYear) {
}

record SyncNoDataCounts(int freshCount, int expiredCount) {
}
