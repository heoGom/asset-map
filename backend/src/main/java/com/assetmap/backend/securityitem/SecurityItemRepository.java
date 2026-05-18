package com.assetmap.backend.securityitem;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityItemRepository extends JpaRepository<SecurityItem, Long> {

	Optional<SecurityItem> findByTicker(String ticker);
}
