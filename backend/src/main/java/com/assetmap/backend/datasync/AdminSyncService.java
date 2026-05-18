package com.assetmap.backend.datasync;

import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.datasync.provider.MarketPriceProvider;
import com.assetmap.backend.datasync.provider.SecurityMasterProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminSyncService {

	private static final String ALL = "ALL";

	private final DataSyncStatusService dataSyncStatusService;
	private final SecurityMasterProvider securityMasterProvider;
	private final MarketPriceProvider marketPriceProvider;
	private final SecurityMasterSyncService securityMasterSyncService;
	private final MarketPriceSyncService marketPriceSyncService;

	public AdminSyncService(
			DataSyncStatusService dataSyncStatusService,
			SecurityMasterProvider securityMasterProvider,
			MarketPriceProvider marketPriceProvider,
			SecurityMasterSyncService securityMasterSyncService,
			MarketPriceSyncService marketPriceSyncService
	) {
		this.dataSyncStatusService = dataSyncStatusService;
		this.securityMasterProvider = securityMasterProvider;
		this.marketPriceProvider = marketPriceProvider;
		this.securityMasterSyncService = securityMasterSyncService;
		this.marketPriceSyncService = marketPriceSyncService;
	}

	public List<DataSyncStatusResponse> getStatuses() {
		return dataSyncStatusService.findAll();
	}

	/**
	 * 개발/관리용 endpoint에서 호출된다. 현재 provider는 Stub이며 실제 KRX HTTP 호출은 승인 후 구현한다.
	 */
	@Transactional
	public AdminSyncResponse syncSecurityMaster(AdminSyncRequest request) {
		boolean force = request != null && request.forceOrFalse();
		if (!dataSyncStatusService.shouldSyncToday(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, force)) {
			DataSyncStatusResponse status = dataSyncStatusService.markSkipped(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, "Already synced today.");
			return response("SKIPPED", DataSyncType.SECURITY_MASTER, ALL, SyncUpsertResultEmpty.INSTANCE, status);
		}

		dataSyncStatusService.markRunning(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, "KRX security master sync stub started.");
		try {
			List<ImportedSecurityMaster> imported = new ArrayList<>();
			imported.addAll(securityMasterProvider.fetchAllKospi());
			imported.addAll(securityMasterProvider.fetchAllKosdaq());
			// TODO: ETF master sync policy will be finalized with KRX response verification.
			imported.addAll(securityMasterProvider.fetchAllEtf());
			SyncUpsertResult result = securityMasterSyncService.upsertImportedSecurities(imported);
			DataSyncStatusResponse status = dataSyncStatusService.markSuccess(
					DataSyncType.SECURITY_MASTER,
					DataSyncSource.KRX,
					ALL,
					LocalDate.now(),
					"KRX security master sync stub completed. No external API is called."
			);
			return response("STUB", DataSyncType.SECURITY_MASTER, ALL, result, status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, ALL, exception.getMessage());
			return response("FAILED", DataSyncType.SECURITY_MASTER, ALL, SyncUpsertResultEmpty.INSTANCE, status);
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
			return response("SKIPPED", DataSyncType.MARKET_PRICE, targetKey, SyncUpsertResultEmpty.INSTANCE, status);
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
			return response("STUB", DataSyncType.MARKET_PRICE, targetKey, result, status);
		} catch (RuntimeException exception) {
			DataSyncStatusResponse status = dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, exception.getMessage());
			return response("FAILED", DataSyncType.MARKET_PRICE, targetKey, SyncUpsertResultEmpty.INSTANCE, status);
		}
	}

	private AdminSyncResponse response(String result, DataSyncType syncType, String targetKey, SyncUpsertResult upsertResult, DataSyncStatusResponse status) {
		return new AdminSyncResponse(
				result,
				syncType,
				DataSyncSource.KRX,
				targetKey,
				upsertResult.receivedCount(),
				upsertResult.insertedCount(),
				upsertResult.updatedCount(),
				upsertResult.skippedCount(),
				status
		);
	}

	private static final class SyncUpsertResultEmpty {
		private static final SyncUpsertResult INSTANCE = new SyncUpsertResult(0, 0, 0, 0);
	}
}
