package com.assetmap.backend.datasync;

public record AdminSyncResponse(
		String result,
		DataSyncType syncType,
		DataSyncSource source,
		String targetKey,
		int receivedCount,
		int insertedCount,
		int updatedCount,
		int skippedCount,
		DataSyncStatusResponse status
) {
}
