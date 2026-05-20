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
		status.markFailed(LocalDateTime.now(), message);
		return DataSyncStatusResponse.from(status);
	}

	@Transactional
	public DataSyncStatusResponse markNoData(DataSyncType syncType, DataSyncSource source, String targetKey, LocalDate checkedDate, String message) {
		DataSyncStatus status = getOrCreate(syncType, source, targetKey);
		status.markNoData(checkedDate, LocalDateTime.now(), message);
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

	public LocalDate lastSuccessDate(DataSyncType syncType, DataSyncSource source, String targetKey) {
		return repository.findBySyncTypeAndSourceAndTargetKey(syncType, source, targetKey)
				.map(DataSyncStatus::getLastSuccessDate)
				.orElse(null);
	}

	public boolean hasNoDataSync(DataSyncType syncType, DataSyncSource source, String targetKey) {
		return repository.findBySyncTypeAndSourceAndTargetKey(syncType, source, targetKey)
				.map(status -> status.getStatus() == DataSyncStatusValue.NO_DATA)
				.orElse(false);
	}

	public boolean hasFreshNoDataSync(DataSyncType syncType, DataSyncSource source, String targetKey, int recheckDays) {
		return repository.findBySyncTypeAndSourceAndTargetKey(syncType, source, targetKey)
				.map(status -> isFreshNoData(status, recheckDays))
				.orElse(false);
	}

	private boolean isFreshNoData(DataSyncStatus status, int recheckDays) {
		if (status.getStatus() != DataSyncStatusValue.NO_DATA || status.getLastSuccessAt() == null) {
			return false;
		}
		if (recheckDays <= 0) {
			return false;
		}
		return status.getLastSuccessAt().isAfter(LocalDateTime.now().minusDays(recheckDays));
	}

	private DataSyncStatus getOrCreate(DataSyncType syncType, DataSyncSource source, String targetKey) {
		return repository.findBySyncTypeAndSourceAndTargetKey(syncType, source, targetKey)
				.orElseGet(() -> repository.save(new DataSyncStatus(syncType, source, targetKey)));
	}
}
