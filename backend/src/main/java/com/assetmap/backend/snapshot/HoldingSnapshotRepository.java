package com.assetmap.backend.snapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoldingSnapshotRepository extends JpaRepository<HoldingSnapshot, Long> {

	List<HoldingSnapshot> findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(Long userId, LocalDate from, LocalDate to);

	Optional<HoldingSnapshot> findByUserIdAndAccountIdAndSecurityItemIdAndSnapshotDate(Long userId, Long accountId, Long securityItemId, LocalDate snapshotDate);

	boolean existsBySnapshotDate(LocalDate snapshotDate);

	@Query("select distinct s.snapshotDate from HoldingSnapshot s where s.snapshotDate between :from and :to")
	List<LocalDate> findDistinctSnapshotDatesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
