package com.assetmap.backend.datasync.execution;

import com.assetmap.backend.datasync.admin.dto.DataSyncStatusResponse;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;

public record StockDividendCheckpointResult(
		Long securityItemId,
		int year,
		String status,
		DividendImportResult importResult,
		int failedCount,
		String message,
		DataSyncStatusResponse dataSyncStatus
) {

	public static StockDividendCheckpointResult success(Long securityItemId, int year, DividendImportResult result, DataSyncStatusResponse status) {
		return new StockDividendCheckpointResult(securityItemId, year, "SUCCESS", result, 0, status.message(), status);
	}

	public static StockDividendCheckpointResult noData(Long securityItemId, int year, DividendImportResult result, DataSyncStatusResponse status) {
		return new StockDividendCheckpointResult(securityItemId, year, "NO_DATA", result, 0, status.message(), status);
	}

	public static StockDividendCheckpointResult failed(Long securityItemId, int year, DividendImportResult result, String message, DataSyncStatusResponse status) {
		return new StockDividendCheckpointResult(securityItemId, year, "FAILED", result, 1, message, status);
	}

	public boolean success() {
		return "SUCCESS".equals(status);
	}
}
