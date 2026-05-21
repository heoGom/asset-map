package com.assetmap.backend.datasync.common;
import com.assetmap.backend.datasync.status.DataSyncStatusService;
import com.assetmap.backend.datasync.common.DataSyncDecision;
import com.assetmap.backend.datasync.common.DataSyncType;
import com.assetmap.backend.datasync.common.DataSyncSource;

import org.springframework.stereotype.Service;

@Service
public class DataSyncPolicyService {

	private final DataSyncStatusService dataSyncStatusService;

	public DataSyncPolicyService(DataSyncStatusService dataSyncStatusService) {
		this.dataSyncStatusService = dataSyncStatusService;
	}

	public DataSyncDecision decideDailySync(
			DataSyncType syncType,
			DataSyncSource source,
			String targetKey,
			boolean force,
			boolean hasRequiredData
	) {
		if (force) {
			return DataSyncDecision.run("Force sync requested.");
		}
		if (!hasRequiredData) {
			return DataSyncDecision.run("Required local data is empty.");
		}
		if (!dataSyncStatusService.shouldSyncToday(syncType, source, targetKey, false)) {
			return DataSyncDecision.skip("Already synced today.");
		}
		return DataSyncDecision.run("Sync is due.");
	}

	public DataSyncDecision decideTargetDateSync(
			DataSyncType syncType,
			DataSyncSource source,
			String targetKey,
			boolean force
	) {
		if (force) {
			return DataSyncDecision.run("Force sync requested.");
		}
		if (dataSyncStatusService.hasSuccessfulSync(syncType, source, targetKey)) {
			return DataSyncDecision.skip("Already synced for target date.");
		}
		return DataSyncDecision.run("Sync is due for target date.");
	}
}
