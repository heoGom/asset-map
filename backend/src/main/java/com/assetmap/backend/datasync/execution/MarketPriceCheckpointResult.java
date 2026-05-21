package com.assetmap.backend.datasync.execution;

import com.assetmap.backend.datasync.admin.dto.DataSyncStatusResponse;
import java.time.LocalDate;

public record MarketPriceCheckpointResult(
		LocalDate priceDate,
		String status,
		SyncUpsertResult upsertResult,
		int importedPriceCount,
		int failedCount,
		String message,
		DataSyncStatusResponse dataSyncStatus
) {

	public static MarketPriceCheckpointResult success(LocalDate priceDate, SyncUpsertResult upsertResult, DataSyncStatusResponse status) {
		return new MarketPriceCheckpointResult(priceDate, "SUCCESS", upsertResult, upsertResult.insertedCount() + upsertResult.updatedCount(), 0, status.message(), status);
	}

	public static MarketPriceCheckpointResult noData(LocalDate priceDate, SyncUpsertResult upsertResult, DataSyncStatusResponse status) {
		return new MarketPriceCheckpointResult(priceDate, "NO_DATA", upsertResult, 0, 0, status.message(), status);
	}

	public static MarketPriceCheckpointResult failed(LocalDate priceDate, String statusText, String message, DataSyncStatusResponse status) {
		return new MarketPriceCheckpointResult(priceDate, statusText, SyncUpsertResult.empty(), 0, 1, message, status);
	}

	public boolean success() {
		return "SUCCESS".equals(status);
	}
}
