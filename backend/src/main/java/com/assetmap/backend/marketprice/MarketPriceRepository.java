package com.assetmap.backend.marketprice;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

	List<MarketPrice> findBySecurityItemIdOrderByPriceDateDesc(Long securityItemId);

	Optional<MarketPrice> findFirstBySecurityItemIdOrderByPriceDateDescFetchedAtDesc(Long securityItemId);
}
