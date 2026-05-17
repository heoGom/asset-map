package com.assetmap.backend.holding;

import java.util.List;
import java.util.Optional;
import com.assetmap.backend.securityitem.SecurityItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

	List<Holding> findByUserId(Long userId);

	List<Holding> findByUserIdAndAccountId(Long userId, Long accountId);

	List<Holding> findBySecurityItemId(Long securityItemId);

	@Query("select distinct h.securityItem from Holding h where h.userId = :userId")
	List<SecurityItem> findDistinctSecurityItemsByUserId(Long userId);

	Optional<Holding> findByUserIdAndAccountIdAndSecurityItemId(Long userId, Long accountId, Long securityItemId);
}
