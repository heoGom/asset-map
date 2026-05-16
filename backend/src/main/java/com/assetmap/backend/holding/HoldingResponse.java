package com.assetmap.backend.holding;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HoldingResponse(
		Long id,
		Long userId,
		Long accountId,
		String accountName,
		Long securityItemId,
		String ticker,
		String securityName,
		BigDecimal quantity,
		BigDecimal averagePrice,
		BigDecimal currentPrice,
		String currency,
		BigDecimal investedAmount,
		BigDecimal evaluatedAmount,
		BigDecimal profitLoss,
		BigDecimal profitLossRate,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static HoldingResponse from(Holding holding) {
		BigDecimal investedAmount = MoneyCalculator.amount(holding.getQuantity(), holding.getAveragePrice());
		BigDecimal evaluatedAmount = MoneyCalculator.amount(holding.getQuantity(), holding.getCurrentPrice());
		BigDecimal profitLoss = evaluatedAmount.subtract(investedAmount);
		return new HoldingResponse(
				holding.getId(),
				holding.getUserId(),
				holding.getAccount().getId(),
				holding.getAccount().getName(),
				holding.getSecurityItem().getId(),
				holding.getSecurityItem().getTicker(),
				holding.getSecurityItem().getName(),
				holding.getQuantity(),
				holding.getAveragePrice(),
				holding.getCurrentPrice(),
				holding.getCurrency(),
				investedAmount,
				evaluatedAmount,
				profitLoss,
				MoneyCalculator.rate(profitLoss, investedAmount),
				holding.getCreatedAt(),
				holding.getUpdatedAt()
		);
	}
}
