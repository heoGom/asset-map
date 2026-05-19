package com.assetmap.backend.datasync;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.datasync.provider.MarketPriceProvider;
import com.assetmap.backend.datasync.provider.SecurityMasterProvider;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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
	private final String defaultSecurityMasterBasDd;

	public AdminSyncService(
			DataSyncStatusService dataSyncStatusService,
			SecurityMasterProvider securityMasterProvider,
			MarketPriceProvider marketPriceProvider,
			SecurityMasterSyncService securityMasterSyncService,
			MarketPriceSyncService marketPriceSyncService,
			@Value("${external.krx.security-master.default-bas-dd:}") String defaultSecurityMasterBasDd
	) {
		this.dataSyncStatusService = dataSyncStatusService;
		this.securityMasterProvider = securityMasterProvider;
		this.marketPriceProvider = marketPriceProvider;
		this.securityMasterSyncService = securityMasterSyncService;
		this.marketPriceSyncService = marketPriceSyncService;
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
		if (!dataSyncStatusService.shouldSyncToday(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, force)) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, "Already synced today.");
			return response("SKIPPED", DataSyncType.SECURITY_MASTER, ALL, compactBasDd, 0, 0, SyncUpsertResultEmpty.INSTANCE, "Already synced today.", status);
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
			return response("SUCCESS", DataSyncType.SECURITY_MASTER, ALL, compactBasDd, kospi.size(), kosdaq.size(), result, message, status);
		} catch (BusinessException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, exception.getMessage());
			return response(exception.getErrorCode().name(), DataSyncType.SECURITY_MASTER, ALL, compactBasDd, 0, 0, SyncUpsertResultEmpty.INSTANCE, exception.getMessage(), status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, exception.getMessage());
			return response("FAILED", DataSyncType.SECURITY_MASTER, ALL, compactBasDd, 0, 0, SyncUpsertResultEmpty.INSTANCE, exception.getMessage(), status);
		}
	}

	/**
	 * 개발/관리용 endpoint에서 호출된다. 현재 provider는 Stub이며 실제 KRX HTTP 호출은 승인 후 구현한다.
	 */
	@Transactional
	public AdminSyncResponse syncMarketPrices(AdminSyncRequest request) {
		boolean force = request != null && request.forceOrFalse();
		LocalDate priceDate = request == null || request.priceDate() == null ? LocalDate.now() : request.priceDate();
		String targetKey = priceDate.toString();
		if (!dataSyncStatusService.shouldSyncToday(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, force)) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, "Already synced today.");
			return response("SKIPPED", DataSyncType.MARKET_PRICE, targetKey, null, 0, 0, SyncUpsertResultEmpty.INSTANCE, "Already synced today.", status);
		}

		dataSyncStatusService.markRunning(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, "KRX market price sync stub started.");
		try {
			List<ImportedMarketPrice> imported = new ArrayList<>();
			imported.addAll(marketPriceProvider.fetchKospiPrices(priceDate));
			imported.addAll(marketPriceProvider.fetchKosdaqPrices(priceDate));
			// TODO: ETF price sync should be limited to held/traded/watchlist ETFs when watchlist exists.
			imported.addAll(marketPriceProvider.fetchEtfPrices(priceDate));
			SyncUpsertResult result = marketPriceSyncService.upsertImportedPrices(imported);
			DataSyncStatusResponse status = dataSyncStatusService.markSuccess(
					DataSyncType.MARKET_PRICE,
					DataSyncSource.KRX,
					targetKey,
					priceDate,
					"KRX market price sync stub completed. No external API is called."
			);
			return response("STUB", DataSyncType.MARKET_PRICE, targetKey, null, 0, 0, result, "KRX market price sync stub completed. No external API is called.", status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, exception.getMessage());
			return response("FAILED", DataSyncType.MARKET_PRICE, targetKey, null, 0, 0, SyncUpsertResultEmpty.INSTANCE, exception.getMessage(), status);
		}
	}

	private AdminSyncResponse response(
			String statusText,
			DataSyncType syncType,
			String targetKey,
			String basDd,
			int kospiImportedCount,
			int kosdaqImportedCount,
			SyncUpsertResult upsertResult,
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
				upsertResult.receivedCount(),
				upsertResult.insertedCount(),
				upsertResult.updatedCount(),
				upsertResult.skippedCount(),
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

	private static final class SyncUpsertResultEmpty {
		private static final SyncUpsertResult INSTANCE = new SyncUpsertResult(0, 0, 0, 0);
	}
}
