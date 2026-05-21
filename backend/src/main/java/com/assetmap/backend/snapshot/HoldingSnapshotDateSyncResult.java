package com.assetmap.backend.snapshot;

import com.assetmap.backend.datasync.admin.dto.DataSyncStatusResponse;
import java.time.LocalDate;

public record HoldingSnapshotDateSyncResult(
		LocalDate snapshotDate,
		String status,
		int targetPositionCount,
		int insertedCount,
		int updatedCount,
		int skippedCount,
		int failedCount,
		String message,
		DataSyncStatusResponse syncStatus
) {

	public boolean success() {
		return failedCount == 0 && ("SUCCESS".equals(status) || "NO_DATA".equals(status));
	}
}
