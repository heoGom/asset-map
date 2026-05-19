package com.assetmap.backend.datasync;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DataSyncStatusService {

	private final DataSyncStatusRepository repository;

	public DataSyncStatusService(DataSyncStatusRepository repository) {
		this.repository = repository;
	}

	public List<DataSyncStatusResponse> findAll() {
		return repository.findAllByOrderBySyncTypeAscSourceAscTargetKeyAsc()
				.stream()
				.map(DataSyncStatusResponse::from)
				.toList();
	}

	@Transactional
	public DataSyncStatusResponse getStatus(DataSyncType syncType, DataSyncSource source, String targetKey) {
		return DataSyncStatusResponse.from(getOrCreate(syncType, source, targetKey));
	}

	@Transactional
	public DataSyncStatusResponse markRunning(DataSyncType syncType, DataSyncSource source, String targetKey, String message) {
		DataSyncStatus status = getOrCreate(syncType, source, targetKey);
		status.markRunning(message);
		return DataSyncStatusResponse.from(status);
	}

	@Transactional
	public DataSyncStatusResponse markSuccess(DataSyncType syncType, DataSyncSource source, String targetKey, LocalDate successDate, String message) {
		DataSyncStatus status = getOrCreate(syncType, source, targetKey);
		status.markSuccess(successDate, LocalDateTime.now(), message);
		return DataSyncStatusResponse.from(status);
	}

	@Transactional
	public DataSyncStatusResponse markFailed(DataSyncType syncType, DataSyncSource source, String targetKey, String message) {
		DataSyncStatus status = getOrCreate(syncType, source, targetKey);
		status.markFailed(message);
		return DataSyncStatusResponse.from(status);
	}

	@Transactional
	public DataSyncStatusResponse markSkipped(DataSyncType syncType, DataSyncSource source, String targetKey, String message) {
		DataSyncStatus status = getOrCreate(syncType, source, targetKey);
		status.markSkipped(message);
		return DataSyncStatusResponse.from(status);
	}

	public boolean shouldSyncToday(DataSyncType syncType, DataSyncSource source, String targetKey, boolean force) {
		if (force) {
			return true;
		}
		return repository.findBySyncTypeAndSourceAndTargetKey(syncType, source, targetKey)
				.map(status -> !LocalDate.now().equals(status.getLastSuccessDate()))
				.orElse(true);
	}

	public boolean hasSuccessfulSync(DataSyncType syncType, DataSyncSource source, String targetKey) {
		return repository.findBySyncTypeAndSourceAndTargetKey(syncType, source, targetKey)
				.map(status -> status.getLastSuccessDate() != null)
				.orElse(false);
	}

	private DataSyncStatus getOrCreate(DataSyncType syncType, DataSyncSource source, String targetKey) {
		return repository.findBySyncTypeAndSourceAndTargetKey(syncType, source, targetKey)
				.orElseGet(() -> repository.save(new DataSyncStatus(syncType, source, targetKey)));
	}
}
