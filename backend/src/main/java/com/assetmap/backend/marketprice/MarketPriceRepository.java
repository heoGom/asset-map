package com.assetmap.backend.marketprice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

	List<MarketPrice> findBySecurityItemIdOrderByPriceDateDesc(Long securityItemId);

	Optional<MarketPrice> findFirstBySecurityItemIdOrderByPriceDateDescFetchedAtDesc(Long securityItemId);

	Optional<MarketPrice> findBySecurityItemIdAndPriceDateAndSource(Long securityItemId, LocalDate priceDate, MarketDataSource source);

	boolean existsBySecurityItemIdAndPriceDateAndSource(Long securityItemId, LocalDate priceDate, MarketDataSource source);

	@Query("select max(m.priceDate) from MarketPrice m where m.source = :source and m.securityItem.id in :securityItemIds")
	Optional<LocalDate> findMaxPriceDateBySourceAndSecurityItemIds(@Param("source") MarketDataSource source, @Param("securityItemIds") List<Long> securityItemIds);

	@Query("select m.securityItem.id from MarketPrice m where m.source = :source and m.priceDate = :priceDate and m.securityItem.id in :securityItemIds")
	List<Long> findSecurityItemIdsWithPrice(
			@Param("source") MarketDataSource source,
			@Param("priceDate") LocalDate priceDate,
			@Param("securityItemIds") List<Long> securityItemIds
	);

	@Query("select count(distinct m.securityItem.id) from MarketPrice m where m.source = :source and m.securityItem.id in :securityItemIds")
	long countDistinctSecurityItemIdsWithPrice(
			@Param("source") MarketDataSource source,
			@Param("securityItemIds") List<Long> securityItemIds
	);
}
