package com.assetmap.backend.datasync;

public record SyncUpsertResult(
		int receivedCount,
		int insertedCount,
		int updatedCount,
		int skippedCount
) {

	public static SyncUpsertResult empty() {
		return new SyncUpsertResult(0, 0, 0, 0);
	}

	public SyncUpsertResult plus(SyncUpsertResult other) {
		return new SyncUpsertResult(
				receivedCount + other.receivedCount,
				insertedCount + other.insertedCount,
				updatedCount + other.updatedCount,
				skippedCount + other.skippedCount
		);
	}
}
