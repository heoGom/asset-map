package com.assetmap.backend.datasync;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSyncStatusRepository extends JpaRepository<DataSyncStatus, Long> {

	Optional<DataSyncStatus> findBySyncTypeAndSourceAndTargetKey(DataSyncType syncType, DataSyncSource source, String targetKey);

	List<DataSyncStatus> findAllByOrderBySyncTypeAscSourceAscTargetKeyAsc();
}
