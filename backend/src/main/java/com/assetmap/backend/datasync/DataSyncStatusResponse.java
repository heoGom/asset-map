package com.assetmap.backend.datasync;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DataSyncStatusResponse(
		Long id,
		DataSyncType syncType,
		DataSyncSource source,
		String targetKey,
		LocalDate lastSuccessDate,
		LocalDateTime lastSuccessAt,
		DataSyncStatusValue status,
		String message
) {

	public static DataSyncStatusResponse from(DataSyncStatus status) {
		return new DataSyncStatusResponse(
				status.getId(),
				status.getSyncType(),
				status.getSource(),
				status.getTargetKey(),
				status.getLastSuccessDate(),
				status.getLastSuccessAt(),
				status.getStatus(),
				status.getMessage()
		);
	}
}
