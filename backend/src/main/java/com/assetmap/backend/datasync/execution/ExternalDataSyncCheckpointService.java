package com.assetmap.backend.datasync.execution;
import com.assetmap.backend.datasync.status.DataSyncStatusService;
import com.assetmap.backend.datasync.admin.AdminSyncService;
import com.assetmap.backend.datasync.execution.StockDividendCheckpointResult;
import com.assetmap.backend.datasync.execution.MarketPriceCheckpointResult;
import com.assetmap.backend.datasync.execution.SyncUpsertResult;
import com.assetmap.backend.datasync.common.DataSyncType;
import com.assetmap.backend.datasync.common.DataSyncSource;
import com.assetmap.backend.datasync.admin.dto.DataSyncStatusResponse;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.datasync.provider.MarketPriceProvider;
import com.assetmap.backend.dividend.common.DataSourceType;
import com.assetmap.backend.dividend.event.DividendEventRepository;
import com.assetmap.backend.dividend.importer.dto.DividendImportRequest;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;
import com.assetmap.backend.dividend.importer.service.DividendEventImportService;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ExternalDataSyncCheckpointService {

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final MarketPriceProvider marketPriceProvider;
	private final MarketPriceSyncService marketPriceSyncService;
	private final DataSyncStatusService dataSyncStatusService;
	private final DividendEventImportService dividendEventImportService;
	private final DividendEventRepository dividendEventRepository;

	public ExternalDataSyncCheckpointService(
			MarketPriceProvider marketPriceProvider,
			MarketPriceSyncService marketPriceSyncService,
			DataSyncStatusService dataSyncStatusService,
			DividendEventImportService dividendEventImportService,
			DividendEventRepository dividendEventRepository
	) {
		this.marketPriceProvider = marketPriceProvider;
		this.marketPriceSyncService = marketPriceSyncService;
		this.dataSyncStatusService = dataSyncStatusService;
		this.dividendEventImportService = dividendEventImportService;
		this.dividendEventRepository = dividendEventRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public MarketPriceCheckpointResult syncMarketPriceDate(LocalDate priceDate, List<SecurityItem> targetSecurities) {
		String targetKey = AdminSyncService.TRADED_SECURITIES + "_" + priceDate.format(COMPACT_DATE);
		dataSyncStatusService.markRunning(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey,
				"KRX market price sync started. basDd=%s target=%d".formatted(priceDate.format(COMPACT_DATE), targetSecurities.size()));
		try {
			List<ImportedMarketPrice> imported = fetchMarketPrices(priceDate, targetSecurities);
			SyncUpsertResult result = marketPriceSyncService.upsertImportedPrices(imported, targetSecurities);
			int changedCount = result.insertedCount() + result.updatedCount();
			if (changedCount == 0) {
				DataSyncStatusResponse status = dataSyncStatusService.markNoData(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, priceDate,
						"NO_DATA: no target market prices for date. target=%d".formatted(targetSecurities.size()));
				return MarketPriceCheckpointResult.noData(priceDate, result, status);
			}
			DataSyncStatusResponse status = dataSyncStatusService.markSuccess(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, priceDate,
					"KRX market price sync completed. imported=%d skipped=%d".formatted(changedCount, result.skippedCount()));
			return MarketPriceCheckpointResult.success(priceDate, result, status);
		} catch (BusinessException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, exception.getMessage());
			return MarketPriceCheckpointResult.failed(priceDate, exception.getErrorCode().name(), exception.getMessage(), status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, exception.getMessage());
			return MarketPriceCheckpointResult.failed(priceDate, "FAILED", exception.getMessage(), status);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public StockDividendCheckpointResult syncStockDividendSecurityYear(SecurityItem securityItem, int year) {
		String targetKey = stockDividendTargetKey(securityItem, year);
		dataSyncStatusService.markRunning(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, targetKey,
				"Stock dividend sync started. securityId=%d year=%d".formatted(securityItem.getId(), year));
		try {
			DividendImportResult result = dividendEventImportService.importTradedStockSecurity(securityItem, new DividendImportRequest(null, year, year));
			if (result.failedSecurityCount() > 0) {
				String message = "Stock dividend sync failed. securityId=%d year=%d failed=%d".formatted(securityItem.getId(), year, result.failedSecurityCount());
				DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, targetKey, message);
				return StockDividendCheckpointResult.failed(securityItem.getId(), year, result, message, status);
			}

			long eventCount = dividendEventRepository.countBySourceAndSecurityItemIdAndDividendYear(
					DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND,
					securityItem.getId(),
					year
			);
			if (eventCount == 0) {
				String message = "NO_DATA: no stock dividend events. securityId=%d year=%d".formatted(securityItem.getId(), year);
				DataSyncStatusResponse status = dataSyncStatusService.markNoData(
						DataSyncType.STOCK_DIVIDEND,
						DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND,
						targetKey,
						LocalDate.of(year, 12, 31),
						message
				);
				return StockDividendCheckpointResult.noData(securityItem.getId(), year, result, status);
			}

			String message = "Stock dividend sync completed. securityId=%d year=%d imported=%d skipped=%d generatedPayments=%d"
					.formatted(securityItem.getId(), year, result.importedEventCount(), result.skippedEventCount(), result.generatedPaymentCount());
			DataSyncStatusResponse status = dataSyncStatusService.markSuccess(
					DataSyncType.STOCK_DIVIDEND,
					DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND,
					targetKey,
					LocalDate.now(),
					message
			);
			return StockDividendCheckpointResult.success(securityItem.getId(), year, result, status);
		} catch (BusinessException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, targetKey, exception.getMessage());
			return StockDividendCheckpointResult.failed(securityItem.getId(), year, DividendImportResult.empty(), exception.getMessage(), status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, targetKey, exception.getMessage());
			return StockDividendCheckpointResult.failed(securityItem.getId(), year, DividendImportResult.empty(), exception.getMessage(), status);
		}
	}

	public static String stockDividendTargetKey(SecurityItem securityItem, int year) {
		return "STOCK_DIVIDEND_%d_%d".formatted(securityItem.getId(), year);
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

	private List<String> targetStockTickers(List<SecurityItem> securities, String market) {
		return securities.stream()
				.filter(security -> security.getSecurityType() == SecurityType.STOCK)
				.filter(security -> StringUtils.hasText(security.getMarket()))
				.filter(security -> security.getMarket().toUpperCase().contains(market))
				.map(SecurityItem::getTicker)
				.toList();
	}
}
