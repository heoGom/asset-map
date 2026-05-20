package com.assetmap.backend.datasync;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.datasync.provider.MarketPriceProvider;
import com.assetmap.backend.datasync.provider.SecurityMasterProvider;
import com.assetmap.backend.dividend.DataSourceType;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.dividend.importer.dto.DividendImportRequest;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;
import com.assetmap.backend.dividend.importer.service.DividendEventImportService;
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
	private final MarketPriceProvider marketPriceProvider;
	private final SecurityMasterSyncService securityMasterSyncService;
	private final MarketPriceSyncService marketPriceSyncService;
	private final SecurityItemRepository securityItemRepository;
	private final TradeTransactionRepository tradeTransactionRepository;
	private final MarketPriceRepository marketPriceRepository;
	private final DividendEventRepository dividendEventRepository;
	private final DividendEventImportService dividendEventImportService;
	private final DataSyncPolicyService dataSyncPolicyService;
	private final String defaultSecurityMasterBasDd;
	private final int marketPriceDefaultLookbackDays;
	private final int marketPriceMaxBackfillDays;
	private final int stockDividendDefaultFromYear;
	private final int stockDividendRecheckYears;

	public AdminSyncService(
			DataSyncStatusService dataSyncStatusService,
			SecurityMasterProvider securityMasterProvider,
			MarketPriceProvider marketPriceProvider,
			SecurityMasterSyncService securityMasterSyncService,
			MarketPriceSyncService marketPriceSyncService,
			SecurityItemRepository securityItemRepository,
			TradeTransactionRepository tradeTransactionRepository,
			MarketPriceRepository marketPriceRepository,
			DividendEventRepository dividendEventRepository,
			DividendEventImportService dividendEventImportService,
			DataSyncPolicyService dataSyncPolicyService,
			@Value("${external.krx.security-master.default-bas-dd:}") String defaultSecurityMasterBasDd,
			@Value("${app.sync.market-prices.default-lookback-days:30}") int marketPriceDefaultLookbackDays,
			@Value("${app.sync.market-prices.max-backfill-days:60}") int marketPriceMaxBackfillDays,
			@Value("${app.sync.stock-dividends.default-from-year:2020}") int stockDividendDefaultFromYear,
			@Value("${app.sync.stock-dividends.recheck-years:2}") int stockDividendRecheckYears
	) {
		this.dataSyncStatusService = dataSyncStatusService;
		this.securityMasterProvider = securityMasterProvider;
		this.marketPriceProvider = marketPriceProvider;
		this.securityMasterSyncService = securityMasterSyncService;
		this.marketPriceSyncService = marketPriceSyncService;
		this.securityItemRepository = securityItemRepository;
		this.tradeTransactionRepository = tradeTransactionRepository;
		this.marketPriceRepository = marketPriceRepository;
		this.dividendEventRepository = dividendEventRepository;
		this.dividendEventImportService = dividendEventImportService;
		this.dataSyncPolicyService = dataSyncPolicyService;
		this.defaultSecurityMasterBasDd = defaultSecurityMasterBasDd;
		this.marketPriceDefaultLookbackDays = marketPriceDefaultLookbackDays;
		this.marketPriceMaxBackfillDays = marketPriceMaxBackfillDays;
		this.stockDividendDefaultFromYear = stockDividendDefaultFromYear;
		this.stockDividendRecheckYears = stockDividendRecheckYears;
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

	@Transactional
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
			LocalDate priceDate = dateTarget.priceDate();
			String dateTargetKey = TRADED_SECURITIES + "_" + priceDate.format(COMPACT_DATE);
			dataSyncStatusService.markRunning(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, dateTargetKey,
					"KRX market price sync started. basDd=%s target=%d".formatted(priceDate.format(COMPACT_DATE), dateTarget.targetSecurities().size()));
			try {
				List<ImportedMarketPrice> imported = fetchMarketPrices(priceDate, dateTarget.targetSecurities());
				SyncUpsertResult result = marketPriceSyncService.upsertImportedPrices(imported, dateTarget.targetSecurities());
				total = total.plus(result);
				int changedCount = result.insertedCount() + result.updatedCount();
				importedPriceCount += changedCount;
				if (changedCount == 0) {
					dataSyncStatusService.markNoData(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, dateTargetKey, priceDate,
							"NO_DATA: no target market prices for date. target=%d".formatted(dateTarget.targetSecurities().size()));
					continue;
				}
				latestSuccessDate = priceDate;
				dataSyncStatusService.markSuccess(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, dateTargetKey, priceDate,
						"KRX market price sync completed. imported=%d skipped=%d".formatted(changedCount, result.skippedCount()));
			} catch (BusinessException exception) {
				failedCount++;
				dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, dateTargetKey, exception.getMessage());
			} catch (RuntimeException exception) {
				failedCount++;
				dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, dateTargetKey, exception.getMessage());
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

	@Transactional
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
		dataSyncStatusService.markRunning(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES,
				"Stock dividend sync started. fromYear=%d toYear=%d target=%d".formatted(range.fromYear(), range.toYear(), targetSecurities.size()));
		try {
			DividendImportResult result = dividendEventImportService.importTradedStockSecurities(new DividendImportRequest(null, range.fromYear(), range.toYear()));
			String message = "Stock dividend sync completed. fromYear=%d toYear=%d target=%d imported=%d skipped=%d generatedPayments=%d failed=%d"
					.formatted(range.fromYear(), range.toYear(), result.targetSecurityCount(), result.importedEventCount(), result.skippedEventCount(), result.generatedPaymentCount(), result.failedSecurityCount());
			DataSyncStatusResponse status;
			String statusText;
			if (result.failedSecurityCount() > 0 && result.importedEventCount() == 0) {
				status = dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, message);
				statusText = "FAILED";
			} else {
				status = dataSyncStatusService.markSuccess(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, LocalDate.now(), message);
				statusText = "SUCCESS";
			}
			return response(statusText, DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES,
					range.fromYear() + ".." + range.toYear(), 0, 0, result.targetSecurityCount(), 0,
					new SyncUpsertResult(result.targetSecurityCount(), result.importedEventCount(), 0, result.skippedEventCount()),
					result.failedSecurityCount(), message, status);
		} catch (BusinessException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, exception.getMessage());
			return response(exception.getErrorCode().name(), DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES,
					range.fromYear() + ".." + range.toYear(), 0, 0, targetSecurities.size(), 0, SyncUpsertResult.empty(), 1, exception.getMessage(), status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES, exception.getMessage());
			return response("FAILED", DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, TRADED_STOCK_SECURITIES,
					range.fromYear() + ".." + range.toYear(), 0, 0, targetSecurities.size(), 0, SyncUpsertResult.empty(), 1, exception.getMessage(), status);
		}
	}

	private List<ImportedMarketPrice> fetchMarketPrices(LocalDate priceDate, List<SecurityItem> targetSecurities) {
		List<ImportedMarketPrice> imported = new ArrayList<>();
		List<String> kospiTickers = targetStockTickers(targetSecurities, "KOSPI");
		List<String> kosdaqTickers = targetStockTickers(targetSecurities, "KOSDAQ");
		List<String> etfTickers = targetSecurities.stream()
				.filter(security -> security.getSecurityType() == SecurityType.ETF)
				.map(SecurityItem::getTicker)
				.toList();
		if (!kospiTickers.isEmpty()) {
			imported.addAll(marketPriceProvider.fetchKospiPrices(priceDate, kospiTickers));
		}
		if (!kosdaqTickers.isEmpty()) {
			imported.addAll(marketPriceProvider.fetchKosdaqPrices(priceDate, kosdaqTickers));
		}
		if (!etfTickers.isEmpty()) {
			imported.addAll(marketPriceProvider.fetchEtfPrices(priceDate, etfTickers));
		}
		return imported;
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
		if (existingIds.isEmpty() && dataSyncStatusService.hasNoDataSync(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, dateTargetKey)) {
			return List.of();
		}
		return eligibleTargets.stream()
				.filter(security -> !existingIds.contains(security.getId()))
				.toList();
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

	private List<String> targetStockTickers(List<SecurityItem> securities, String market) {
		return securities.stream()
				.filter(security -> security.getSecurityType() == SecurityType.STOCK)
				.filter(security -> StringUtils.hasText(security.getMarket()))
				.filter(security -> security.getMarket().toUpperCase().contains(market))
				.map(SecurityItem::getTicker)
				.toList();
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

	private record YearRange(int fromYear, int toYear) {
	}
}
