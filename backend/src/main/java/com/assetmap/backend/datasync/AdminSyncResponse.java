package com.assetmap.backend.datasync;

public record AdminSyncResponse(
		String status,
		DataSyncType syncType,
		DataSyncSource source,
		String targetKey,
		String basDd,
		int kospiImportedCount,
		int kosdaqImportedCount,
		int targetSecurityCount,
		int importedPriceCount,
		int receivedCount,
		int insertedCount,
		int updatedCount,
		int skippedCount,
		int failedCount,
		String message,
		DataSyncStatusResponse syncStatus
) {
}
