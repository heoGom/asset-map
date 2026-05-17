package com.assetmap.backend.holding;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

	List<Holding> findByUserId(Long userId);

	List<Holding> findByUserIdAndAccountId(Long userId, Long accountId);

	Optional<Holding> findByUserIdAndAccountIdAndSecurityItemId(Long userId, Long accountId, Long securityItemId);
}
