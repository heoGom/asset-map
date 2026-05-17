package com.assetmap.backend.dividend;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DividendEventRepository extends JpaRepository<DividendEvent, Long> {

	List<DividendEvent> findBySecurityItemId(Long securityItemId);

	List<DividendEvent> findBySecurityItemIdAndDividendYear(Long securityItemId, Integer dividendYear);

	boolean existsBySecurityItemIdAndRecordDateAndPaymentDateAndDividendPerShareAndSource(Long securityItemId, LocalDate recordDate, LocalDate paymentDate, BigDecimal dividendPerShare, DataSourceType source);
}
