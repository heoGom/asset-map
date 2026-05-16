package com.assetmap.backend.transaction;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HoldingAdjustmentService {

	private static final int PRICE_SCALE = 6;

	private final HoldingRepository holdingRepository;
	private final TradeTransactionRepository transactionRepository;

	public HoldingAdjustmentService(HoldingRepository holdingRepository, TradeTransactionRepository transactionRepository) {
		this.holdingRepository = holdingRepository;
		this.transactionRepository = transactionRepository;
	}

	public void validateSellable(Long userId, Long accountId, Long securityItemId, BigDecimal quantity) {
		BigDecimal currentQuantity = holdingRepository.findByUserIdAndAccountIdAndSecurityItemId(userId, accountId, securityItemId)
				.map(Holding::getQuantity)
				.orElse(BigDecimal.ZERO);
		if (currentQuantity.compareTo(quantity) < 0) {
			throw new BusinessException(ErrorCode.COMMON_001);
		}
	}

	public void rebuild(Long userId, Long accountId, Long securityItemId) {
		List<TradeTransaction> transactions = transactionRepository.findByUserIdAndAccountIdAndSecurityItemIdOrderByTradeDateAscIdAsc(userId, accountId, securityItemId);
		Position position = calculatePosition(transactions);
		Holding holding = holdingRepository.findByUserIdAndAccountIdAndSecurityItemId(userId, accountId, securityItemId).orElse(null);

		if (holding == null && position.quantity().compareTo(BigDecimal.ZERO) == 0) {
			return;
		}

		if (holding == null) {
			TradeTransaction last = transactions.get(transactions.size() - 1);
			holdingRepository.save(new Holding(userId, last.getAccount(), last.getSecurityItem(), position.quantity(), position.averagePrice(), position.currentPrice(), last.getCurrency()));
			return;
		}

		String currency = transactions.isEmpty() ? holding.getCurrency() : transactions.get(transactions.size() - 1).getCurrency();
		holding.replacePosition(position.quantity(), position.averagePrice(), position.currentPrice(), currency);
	}

	private Position calculatePosition(List<TradeTransaction> transactions) {
		BigDecimal quantity = BigDecimal.ZERO;
		BigDecimal averagePrice = BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
		BigDecimal currentPrice = BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP);

		for (TradeTransaction transaction : transactions) {
			currentPrice = transaction.getPrice();
			if (transaction.getTradeType() == TradeType.SELL) {
				quantity = quantity.subtract(transaction.getQuantity());
				if (quantity.compareTo(BigDecimal.ZERO) < 0) {
					throw new BusinessException(ErrorCode.COMMON_001);
				}
				continue;
			}
			BigDecimal oldCost = quantity.multiply(averagePrice);
			BigDecimal newCost = transaction.getQuantity().multiply(transaction.getPrice());
			BigDecimal newQuantity = quantity.add(transaction.getQuantity());
			averagePrice = newQuantity.compareTo(BigDecimal.ZERO) == 0
					? BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP)
					: oldCost.add(newCost).divide(newQuantity, PRICE_SCALE, RoundingMode.HALF_UP);
			quantity = newQuantity;
		}

		if (quantity.compareTo(BigDecimal.ZERO) == 0) {
			averagePrice = BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
		}
		return new Position(quantity, averagePrice, currentPrice);
	}

	private record Position(BigDecimal quantity, BigDecimal averagePrice, BigDecimal currentPrice) {
	}
}
