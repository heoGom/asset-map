package com.assetmap.backend.datasync;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSyncStatusRepository extends JpaRepository<DataSyncStatus, Long> {

	Optional<DataSyncStatus> findBySyncTypeAndSourceAndTargetKey(DataSyncType syncType, DataSyncSource source, String targetKey);

	List<DataSyncStatus> findBySyncTypeAndSource(DataSyncType syncType, DataSyncSource source);

	List<DataSyncStatus> findBySyncTypeAndSourceAndStatusAndTargetKeyStartingWithOrderByLastFailureAtDesc(
			DataSyncType syncType,
			DataSyncSource source,
			DataSyncStatusValue status,
			String targetKeyPrefix
	);

	List<DataSyncStatus> findBySyncTypeAndSourceAndStatusAndTargetKeyStartingWith(
			DataSyncType syncType,
			DataSyncSource source,
			DataSyncStatusValue status,
			String targetKeyPrefix
	);

	List<DataSyncStatus> findAllByOrderBySyncTypeAscSourceAscTargetKeyAsc();
}
