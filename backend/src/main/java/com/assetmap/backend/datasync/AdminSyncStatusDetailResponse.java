package com.assetmap.backend.datasync;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminSyncStatusDetailResponse(
		SyncStatusSummary securityMaster,
		MarketPriceSyncStatus marketPrices,
		StockDividendSyncStatus stockDividends,
		List<DataSyncStatusResponse> rawStatuses
) {

	public record SyncStatusSummary(
			DataSyncType syncType,
			DataSyncSource source,
			String targetKey,
			LocalDateTime lastSuccessAt,
			LocalDateTime lastFailureAt,
			DataSyncStatusValue status,
			String message
	) {
	}

	public record MarketPriceSyncStatus(
			int totalTargetSecurityCount,
			int pricedSecurityCount,
			int missingSecurityCount,
			int pendingDateCount,
			int freshNoDataDateCount,
			int expiredNoDataDateCount,
			LocalDateTime lastSuccessAt,
			LocalDateTime lastFailureAt,
			List<MarketPriceFailedDate> recentFailedDates
	) {
	}

	public record MarketPriceFailedDate(
			String targetKey,
			LocalDate priceDate,
			LocalDateTime lastFailureAt,
			String message
	) {
	}

	public record StockDividendSyncStatus(
			int totalTargetSecurityCount,
			int eventSecurityCount,
			int missingOrRecheckSecurityYearCount,
			int freshNoDataSecurityYearCount,
			int expiredNoDataSecurityYearCount,
			LocalDateTime lastSuccessAt,
			LocalDateTime lastFailureAt,
			List<StockDividendFailedSecurityYear> recentFailedSecurityYears
	) {
	}

	public record StockDividendFailedSecurityYear(
			String targetKey,
			Long securityItemId,
			Integer year,
			LocalDateTime lastFailureAt,
			String message
	) {
	}
}
