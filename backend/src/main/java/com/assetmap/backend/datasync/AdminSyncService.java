package com.assetmap.backend.datasync;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.datasync.provider.MarketPriceProvider;
import com.assetmap.backend.datasync.provider.SecurityMasterProvider;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemRepository;
import com.assetmap.backend.securityitem.SecurityType;
import com.assetmap.backend.transaction.TradeTransactionRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AdminSyncService {

	private static final String ALL = "ALL";
	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final DataSyncStatusService dataSyncStatusService;
	private final SecurityMasterProvider securityMasterProvider;
	private final MarketPriceProvider marketPriceProvider;
	private final SecurityMasterSyncService securityMasterSyncService;
	private final MarketPriceSyncService marketPriceSyncService;
	private final SecurityItemRepository securityItemRepository;
	private final HoldingRepository holdingRepository;
	private final TradeTransactionRepository tradeTransactionRepository;
	private final DataSyncPolicyService dataSyncPolicyService;
	private final String defaultSecurityMasterBasDd;

	public AdminSyncService(
			DataSyncStatusService dataSyncStatusService,
			SecurityMasterProvider securityMasterProvider,
			MarketPriceProvider marketPriceProvider,
			SecurityMasterSyncService securityMasterSyncService,
			MarketPriceSyncService marketPriceSyncService,
			SecurityItemRepository securityItemRepository,
			HoldingRepository holdingRepository,
			TradeTransactionRepository tradeTransactionRepository,
			DataSyncPolicyService dataSyncPolicyService,
			@Value("${external.krx.security-master.default-bas-dd:}") String defaultSecurityMasterBasDd
	) {
		this.dataSyncStatusService = dataSyncStatusService;
		this.securityMasterProvider = securityMasterProvider;
		this.marketPriceProvider = marketPriceProvider;
		this.securityMasterSyncService = securityMasterSyncService;
		this.marketPriceSyncService = marketPriceSyncService;
		this.securityItemRepository = securityItemRepository;
		this.holdingRepository = holdingRepository;
		this.tradeTransactionRepository = tradeTransactionRepository;
		this.dataSyncPolicyService = dataSyncPolicyService;
		this.defaultSecurityMasterBasDd = defaultSecurityMasterBasDd;
	}

	public List<DataSyncStatusResponse> getStatuses() {
		return dataSyncStatusService.findAll();
	}

	@Transactional
	public AdminSyncResponse syncSecurityMaster(AdminSyncRequest request) {
		boolean force = request != null && request.forceOrFalse();
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
			return response("SKIPPED", DataSyncType.SECURITY_MASTER, ALL, compactBasDd, 0, 0, 0, 0, SyncUpsertResultEmpty.INSTANCE, 0, decision.message(), status);
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
			return response("SUCCESS", DataSyncType.SECURITY_MASTER, ALL, compactBasDd, kospi.size(), kosdaq.size(), 0, 0, result, 0, message, status);
		} catch (BusinessException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, exception.getMessage());
			return response(exception.getErrorCode().name(), DataSyncType.SECURITY_MASTER, ALL, compactBasDd, 0, 0, 0, 0, SyncUpsertResultEmpty.INSTANCE, 1, exception.getMessage(), status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, exception.getMessage());
			return response("FAILED", DataSyncType.SECURITY_MASTER, ALL, compactBasDd, 0, 0, 0, 0, SyncUpsertResultEmpty.INSTANCE, 1, exception.getMessage(), status);
		}
	}

	@Transactional
	public AdminSyncResponse syncMarketPrices(Long userId, AdminSyncRequest request) {
		boolean force = request != null && request.forceOrFalse();
		LocalDate priceDate = resolveMarketPriceBasDd(request);
		String compactBasDd = priceDate.format(COMPACT_DATE);
		String targetKey = compactBasDd;
		List<SecurityItem> targetSecurities = targetSecurities(userId);
		if (targetSecurities.isEmpty()) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, "No target securities for current user.");
			return response("SKIPPED", DataSyncType.MARKET_PRICE, targetKey, compactBasDd, 0, 0, 0, 0, SyncUpsertResultEmpty.INSTANCE, 0, "No target securities for current user.", status);
		}
		DataSyncDecision decision = dataSyncPolicyService.decideTargetDateSync(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, force);
		if (!decision.shouldRun()) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, decision.message());
			return response("SKIPPED", DataSyncType.MARKET_PRICE, targetKey, compactBasDd, 0, 0, targetSecurities.size(), 0, SyncUpsertResultEmpty.INSTANCE, 0, decision.message(), status);
		}

		dataSyncStatusService.markRunning(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, "KRX market price sync started. basDd=" + compactBasDd);
		try {
			List<ImportedMarketPrice> imported = new ArrayList<>();
			List<String> kospiTickers = targetTickers(targetSecurities, "KOSPI");
			List<String> kosdaqTickers = targetTickers(targetSecurities, "KOSDAQ");
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
			SyncUpsertResult result = marketPriceSyncService.upsertImportedPrices(imported, targetSecurities);
			int importedPriceCount = result.insertedCount() + result.updatedCount();
			String message = "KRX market price sync completed. basDd=%s target=%d imported=%d skipped=%d"
					.formatted(compactBasDd, targetSecurities.size(), importedPriceCount, result.skippedCount());
			DataSyncStatusResponse status = dataSyncStatusService.markSuccess(
					DataSyncType.MARKET_PRICE,
					DataSyncSource.KRX,
					targetKey,
					priceDate,
					message
			);
			return response("SUCCESS", DataSyncType.MARKET_PRICE, targetKey, compactBasDd, 0, 0, targetSecurities.size(), importedPriceCount, result, 0, message, status);
		} catch (BusinessException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, exception.getMessage());
			return response(exception.getErrorCode().name(), DataSyncType.MARKET_PRICE, targetKey, compactBasDd, 0, 0, targetSecurities.size(), 0, SyncUpsertResultEmpty.INSTANCE, 1, exception.getMessage(), status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, exception.getMessage());
			return response("FAILED", DataSyncType.MARKET_PRICE, targetKey, compactBasDd, 0, 0, targetSecurities.size(), 0, SyncUpsertResultEmpty.INSTANCE, 1, exception.getMessage(), status);
		}
	}

	private AdminSyncResponse response(
			String statusText,
			DataSyncType syncType,
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
				DataSyncSource.KRX,
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

	private LocalDate resolveMarketPriceBasDd(AdminSyncRequest request) {
		if (request != null && request.basDd() != null) {
			return request.basDd();
		}
		if (request != null && request.priceDate() != null) {
			return request.priceDate();
		}
		return LocalDate.now();
	}

	private List<SecurityItem> targetSecurities(Long userId) {
		Map<Long, SecurityItem> targets = new LinkedHashMap<>();
		holdingRepository.findDistinctSecurityItemsByUserId(userId)
				.forEach(security -> targets.put(security.getId(), security));
		tradeTransactionRepository.findDistinctSecurityItemsByUserId(userId)
				.forEach(security -> targets.put(security.getId(), security));
		return targets.values().stream()
				.filter(security -> security.getSecurityType() == SecurityType.STOCK || security.getSecurityType() == SecurityType.ETF)
				.toList();
	}

	private List<String> targetTickers(List<SecurityItem> securities, String market) {
		return securities.stream()
				.filter(security -> security.getSecurityType() == SecurityType.STOCK)
				.filter(security -> StringUtils.hasText(security.getMarket()))
				.filter(security -> security.getMarket().toUpperCase().contains(market))
				.map(SecurityItem::getTicker)
				.toList();
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

	private static final class SyncUpsertResultEmpty {
		private static final SyncUpsertResult INSTANCE = new SyncUpsertResult(0, 0, 0, 0);
	}
}
