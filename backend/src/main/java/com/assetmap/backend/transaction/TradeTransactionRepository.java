package com.assetmap.backend.transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

	List<TradeTransaction> findByUserIdOrderByTradeDateAscIdAsc(Long userId);

	Optional<TradeTransaction> findByIdAndUserId(Long id, Long userId);

	List<TradeTransaction> findByUserIdAndAccountIdAndSecurityItemIdOrderByTradeDateAscIdAsc(Long userId, Long accountId, Long securityItemId);

	List<TradeTransaction> findByUserIdAndAccountIdAndSecurityItemIdAndTradeDateLessThanEqualOrderByTradeDateAscIdAsc(Long userId, Long accountId, Long securityItemId, LocalDate tradeDate);
}
