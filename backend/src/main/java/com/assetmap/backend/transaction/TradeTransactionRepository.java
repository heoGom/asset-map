package com.assetmap.backend.transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

	List<TradeTransaction> findByUserIdOrderByTradeDateAscIdAsc(Long userId);

	List<TradeTransaction> findByUserIdAndAccountIdOrderByTradeDateAscIdAsc(Long userId, Long accountId);

	Optional<TradeTransaction> findByIdAndUserId(Long id, Long userId);

	List<TradeTransaction> findByUserIdAndAccountIdAndSecurityItemIdOrderByTradeDateAscIdAsc(Long userId, Long accountId, Long securityItemId);

	List<TradeTransaction> findByUserIdAndAccountIdAndSecurityItemIdAndTradeDateLessThanEqualOrderByTradeDateAscIdAsc(Long userId, Long accountId, Long securityItemId, LocalDate tradeDate);

	@Query("select distinct t.securityItem from TradeTransaction t where t.userId = :userId")
	List<SecurityItem> findDistinctSecurityItemsByUserId(Long userId);

	@Query("select distinct t.securityItem from TradeTransaction t where t.securityItem.securityType in :securityTypes")
	List<SecurityItem> findDistinctSecurityItemsBySecurityTypes(@Param("securityTypes") List<SecurityType> securityTypes);

	@Query("select distinct t.securityItem from TradeTransaction t where t.userId = :userId and t.securityItem.securityType in :securityTypes")
	List<SecurityItem> findDistinctSecurityItemsByUserIdAndSecurityTypes(@Param("userId") Long userId, @Param("securityTypes") List<SecurityType> securityTypes);

	@Query("select distinct t.userId from TradeTransaction t where t.securityItem.id = :securityItemId")
	List<Long> findDistinctUserIdsBySecurityItemId(@Param("securityItemId") Long securityItemId);
}
