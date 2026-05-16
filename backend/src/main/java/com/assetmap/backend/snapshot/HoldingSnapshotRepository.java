package com.assetmap.backend.snapshot;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingSnapshotRepository extends JpaRepository<HoldingSnapshot, Long> {

	List<HoldingSnapshot> findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(Long userId, LocalDate from, LocalDate to);
}
