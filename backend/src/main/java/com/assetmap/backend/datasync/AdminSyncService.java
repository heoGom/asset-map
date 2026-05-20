package com.assetmap.backend.datasync;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.datasync.provider.SecurityMasterProvider;
import com.assetmap.backend.dividend.DataSourceType;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPriceRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemRepository;
import com.assetmap.backend.securityitem.SecurityType;
import com.assetmap.backend.transaction.SecurityTradeStartProjection;
import com.assetmap.backend.transaction.TradeTransactionRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AdminSyncService {

	static final String ALL = "ALL";
	static final String TRADED_SECURITIES = "TRADED_SECURITIES";
	static final String TRADED_STOCK_SECURITIES = "TRADED_STOCK_SECURITIES";

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final DataSyncStatusService dataSyncStatusService;
	private final SecurityMasterProvider securityMasterProvider;
	private final SecurityMasterSyncService securityMasterSyncService;
	private final ExternalDataSyncCheckpointService checkpointService;
	private final SecurityItemRepository securityItemRepository;
	private final TradeTransactionRepository tradeTransactionRepository;
	private final MarketPriceRepository marketPriceRepository;
	private final DividendEventRepository dividendEventRepository;
	private final DataSyncPolicyService dataSyncPolicyService;
	private final String defaultSecurityMasterBasDd;
	private final int marketPriceDefaultLookbackDays;
	private final int marketPriceMaxBackfillDays;
	private final int stockDividendDefaultFromYear;
	private final int stockDividendRecheckYears;
	private final int noDataRecheckDays;

	public AdminSyncService(
			DataSyncStatusService dataSyncStatusService,
			SecurityMasterProvider securityMasterProvider,
			SecurityMasterSyncService securityMasterSyncService,
			ExternalDataSyncCheckpointService checkpointService,
			SecurityItemRepository securityItemRepository,
			TradeTransactionRepository tradeTransactionRepository,
			MarketPriceRepository marketPriceRepository,
			DividendEventRepository dividendEventRepository,
			DataSyncPolicyService dataSyncPolicyService,
			@Value("${external.krx.security-master.default-bas-dd:}") String defaultSecurityMasterBasDd,
			@Value("${app.sync.market-prices.default-lookback-days:30}") int marketPriceDefaultLookbackDays,
			@Value("${app.sync.market-prices.max-backfill-days:60}") int marketPriceMaxBackfillDays,
			@Value("${app.sync.stock-dividends.default-from-year:2020}") int stockDividendDefaultFromYear,
			@Value("${app.sync.stock-dividends.recheck-years:2}") int stockDividendRecheckYears,
			@Value("${app.sync.no-data-recheck-days:7}") int noDataRecheckDays
	) {
		this.dataSyncStatusService = dataSyncStatusService;
		this.securityMasterProvider = securityMasterProvider;
		this.securityMasterSyncService = securityMasterSyncService;
		this.checkpointService = checkpointService;
		this.securityItemRepository = securityItemRepository;
		this.tradeTransactionRepository = tradeTransactionRepository;
		this.marketPriceRepository = marketPriceRepository;
		this.dividendEventRepository = dividendEventRepository;
		this.dataSyncPolicyService = dataSyncPolicyService;
		this.defaultSecurityMasterBasDd = defaultSecurityMasterBasDd;
		this.marketPriceDefaultLookbackDays = marketPriceDefaultLookbackDays;
		this.marketPriceMaxBackfillDays = marketPriceMaxBackfillDays;
		this.stockDividendDefaultFromYear = stockDividendDefaultFromYear;
		this.stockDividendRecheckYears = stockDividendRecheckYears;
		this.noDataRecheckDays = noDataRecheckDays;
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
		List<SecurityItem> targetSecurities = tradeTransactionRepository.findDistinctSecurityItemsBySecurityTypes(List.of(SecurityType.STOCK, SecurityType.ETF));
		if (targetSecurities.isEmpty()) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, "No traded STOCK/ETF securities.");
			return response("SKIPPED", DataSyncType.MARKET_PRICE, DataSyncSource.KRX, TRADED_SECURITIES, "", 0, 0, 0, 0, SyncUpsertResult.empty(), 0, "No traded STOCK/ETF securities.", status);
		}

		List<MarketPriceDateTarget> dateTargets = resolveMarketPriceDateTargets(request, targetSecurities);
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
		List<SecurityItem> targetSecurities = tradeTransactionRepository.findDistinctSecurityItemsBySecurityTypes(List.of(SecurityType.STOCK))
				.stream()
				.filter(this::isDomesticKrwStock)
				.toList();
		if (targetSecurities.isEmpty()) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, "No traded STOCK securities.");
			return response("SKIPPED", DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, "", 0, 0, 0, 0, SyncUpsertResult.empty(), 0, "No traded STOCK securities.", status);
		}

		YearRange range = resolveStockDividendRange(request, force, targetSecurities);
		List<StockDividendYearTarget> yearTargets = resolveStockDividendYearTargets(request, force, targetSecurities, range);
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

	private List<MarketPriceDateTarget> resolveMarketPriceDateTargets(AdminSyncRequest request, List<SecurityItem> targetSecurities) {
		LocalDate explicitDate = resolveExplicitMarketPriceDate(request);
		Map<Long, LocalDate> firstTradeDateBySecurityId = firstTradeDateBySecurityId(List.of(SecurityType.STOCK, SecurityType.ETF));
		if (explicitDate != null) {
			List<SecurityItem> missingTargets = missingMarketPriceTargets(explicitDate, targetSecurities, firstTradeDateBySecurityId, force(request));
			return missingTargets.isEmpty() ? List.of() : List.of(new MarketPriceDateTarget(explicitDate, missingTargets));
		}
		LocalDate end = LocalDate.now();
		if (request != null && request.basDd() != null) {
			end = request.basDd();
		}
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
		boolean force = force(request);
		int configuredChunkDays = marketPriceMaxBackfillDays > 0 ? marketPriceMaxBackfillDays : marketPriceDefaultLookbackDays;
		int maxDatesPerRun = Math.max(1, configuredChunkDays);
		List<MarketPriceDateTarget> dateTargets = new ArrayList<>();
		for (LocalDate priceDate = start; !priceDate.isAfter(syncEnd); priceDate = priceDate.plusDays(1)) {
			List<SecurityItem> missingTargets = missingMarketPriceTargets(priceDate, targetSecurities, firstTradeDateBySecurityId, force);
			if (missingTargets.isEmpty()) {
				continue;
			}
			dateTargets.add(new MarketPriceDateTarget(priceDate, missingTargets));
			if (dateTargets.size() >= maxDatesPerRun) {
				break;
			}
		}
		return dateTargets;
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
		int recheckFromYear = Math.max(fullFromYear, currentYear - Math.max(1, stockDividendRecheckYears) + 1);
		int fromYear = hasHistoricalDividendGap(targetSecurities, firstTradeDateBySecurityId, fullFromYear, recheckFromYear - 1)
				? fullFromYear
				: recheckFromYear;
		return new YearRange(fromYear, currentYear);
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
		String dateTargetKey = TRADED_SECURITIES + "_" + priceDate.format(COMPACT_DATE);
		if (existingIds.isEmpty() && dataSyncStatusService.hasFreshNoDataSync(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, dateTargetKey, noDataRecheckDays)) {
			return List.of();
		}
		return eligibleTargets.stream()
				.filter(security -> !existingIds.contains(security.getId()))
				.toList();
	}

	private List<StockDividendYearTarget> resolveStockDividendYearTargets(
			AdminSyncRequest request,
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
				if (shouldSyncStockDividendYear(securityItem, year, force(request) || force, recentRecheckYear)) {
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

	private Map<Long, LocalDate> firstTradeDateBySecurityId(List<SecurityType> securityTypes) {
		Map<Long, LocalDate> firstTradeDateBySecurityId = new HashMap<>();
		for (SecurityTradeStartProjection projection : tradeTransactionRepository.findFirstTradeDateBySecurityTypes(securityTypes)) {
			firstTradeDateBySecurityId.put(projection.getSecurityItemId(), projection.getFirstTradeDate());
		}
		return firstTradeDateBySecurityId;
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

	private LocalDate resolveExplicitMarketPriceDate(AdminSyncRequest request) {
		if (request == null) {
			return null;
		}
		if (request.priceDate() != null) {
			return request.priceDate();
		}
		return request.basDd();
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
		String country = securityItem.getCountry().replaceAll("[\\s\\-_/().]", "").toUpperCase();
		return country.equals("KR") || country.equals("KOREA") || country.equals("SOUTHKOREA") || country.equals("대한민국");
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

	private record MarketPriceDateTarget(LocalDate priceDate, List<SecurityItem> targetSecurities) {
	}

	private record StockDividendYearTarget(SecurityItem securityItem, int year) {
	}

	private record YearRange(int fromYear, int toYear) {
	}
}
