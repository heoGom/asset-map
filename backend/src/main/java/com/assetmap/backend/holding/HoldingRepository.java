package com.assetmap.backend.holding;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

	List<Holding> findByUserId(Long userId);
}
