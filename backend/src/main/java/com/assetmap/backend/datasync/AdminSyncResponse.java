package com.assetmap.backend.datasync;

public record AdminSyncResponse(
		String status,
		DataSyncType syncType,
		DataSyncSource source,
		String targetKey,
		String basDd,
		int kospiImportedCount,
		int kosdaqImportedCount,
		int receivedCount,
		int insertedCount,
		int updatedCount,
		int skippedCount,
		String message,
		DataSyncStatusResponse syncStatus
) {
}
