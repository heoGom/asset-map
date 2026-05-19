package com.assetmap.backend.dividend;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DividendEventRepository extends JpaRepository<DividendEvent, Long> {

	List<DividendEvent> findBySecurityItemId(Long securityItemId);

	List<DividendEvent> findBySecurityItemIdAndDividendYear(Long securityItemId, Integer dividendYear);

	boolean existsBySecurityItemIdAndRecordDateAndPaymentDateAndDividendPerShareAndSource(Long securityItemId, LocalDate recordDate, LocalDate paymentDate, BigDecimal dividendPerShare, DataSourceType source);

	boolean existsBySecurityItemIdAndRecordDateAndDividendPerShareAndSource(Long securityItemId, LocalDate recordDate, BigDecimal dividendPerShare, DataSourceType source);

	Optional<DividendEvent> findFirstBySecurityItemIdAndRecordDateAndDividendPerShareAndSource(Long securityItemId, LocalDate recordDate, BigDecimal dividendPerShare, DataSourceType source);

	@Query("select count(e) from DividendEvent e where e.source = :source and e.securityItem.id in :securityItemIds")
	long countBySourceAndSecurityItemIds(@Param("source") DataSourceType source, @Param("securityItemIds") List<Long> securityItemIds);
}
