package com.assetmap.backend.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class PositionCalculationService {

	private final TradeTransactionRepository transactionRepository;

	public PositionCalculationService(TradeTransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	public BigDecimal quantityAt(Long userId, Long accountId, Long securityItemId, LocalDate recordDate) {
		BigDecimal quantity = BigDecimal.ZERO;
		for (TradeTransaction transaction : transactionRepository.findByUserIdAndAccountIdAndSecurityItemIdAndTradeDateLessThanEqualOrderByTradeDateAscIdAsc(userId, accountId, securityItemId, recordDate)) {
			if (transaction.getTradeType() == TradeType.SELL) {
				quantity = quantity.subtract(transaction.getQuantity());
			} else {
				quantity = quantity.add(transaction.getQuantity());
			}
		}
		return quantity.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : quantity;
	}
}
