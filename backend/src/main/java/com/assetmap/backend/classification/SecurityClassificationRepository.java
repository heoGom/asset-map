package com.assetmap.backend.classification;

import com.assetmap.backend.securityitem.SecurityItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityClassificationRepository extends JpaRepository<SecurityClassification, Long> {

	boolean existsBySecurityItem(SecurityItem securityItem);

	Optional<SecurityClassification> findBySecurityItemId(Long securityItemId);
}
