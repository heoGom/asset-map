package com.assetmap.backend.datasync.admin.dto;
import com.assetmap.backend.datasync.common.DataSyncType;
import com.assetmap.backend.datasync.status.enums.DataSyncStatusValue;
import com.assetmap.backend.datasync.common.DataSyncSource;
import com.assetmap.backend.datasync.status.DataSyncStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DataSyncStatusResponse(
		Long id,
		DataSyncType syncType,
		DataSyncSource source,
		String targetKey,
		LocalDate lastSuccessDate,
		LocalDateTime lastSuccessAt,
		LocalDateTime lastFailureAt,
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
				status.getLastFailureAt(),
				status.getStatus(),
				status.getMessage()
		);
	}
}
