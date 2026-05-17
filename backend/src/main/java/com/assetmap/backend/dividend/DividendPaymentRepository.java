package com.assetmap.backend.dividend;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DividendPaymentRepository extends JpaRepository<DividendPayment, Long> {

	List<DividendPayment> findByUserId(Long userId);

	List<DividendPayment> findByUserIdAndAccountIdOrderByPaymentDateAscIdAsc(Long userId, Long accountId);

	List<DividendPayment> findByUserIdAndStatus(Long userId, DividendPaymentStatus status);

	List<DividendPayment> findByUserIdAndStatusAndPaymentDateBetween(Long userId, DividendPaymentStatus status, LocalDate from, LocalDate to);

	Optional<DividendPayment> findByUserIdAndAccountIdAndSecurityItemIdAndDividendEventId(Long userId, Long accountId, Long securityItemId, Long dividendEventId);
}
