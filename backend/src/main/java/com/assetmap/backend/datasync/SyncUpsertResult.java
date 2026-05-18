package com.assetmap.backend.datasync;

public record SyncUpsertResult(
		int receivedCount,
		int insertedCount,
		int updatedCount,
		int skippedCount
) {
}
