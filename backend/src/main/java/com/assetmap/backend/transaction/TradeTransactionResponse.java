package com.assetmap.backend.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TradeTransactionResponse(
		Long id,
		Long userId,
		Long accountId,
		String accountName,
		Long securityItemId,
		String ticker,
		String securityName,
		LocalDate tradeDate,
		TradeType tradeType,
		BigDecimal quantity,
		BigDecimal price,
		BigDecimal grossAmount,
		BigDecimal fee,
		BigDecimal tax,
		BigDecimal netAmount,
		String currency,
		TransactionSource source,
		String memo,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static TradeTransactionResponse from(TradeTransaction transaction) {
		return new TradeTransactionResponse(
				transaction.getId(),
				transaction.getUserId(),
				transaction.getAccount().getId(),
				transaction.getAccount().getName(),
				transaction.getSecurityItem().getId(),
				transaction.getSecurityItem().getTicker(),
				transaction.getSecurityItem().getName(),
				transaction.getTradeDate(),
				transaction.getTradeType(),
				transaction.getQuantity(),
				transaction.getPrice(),
				transaction.getGrossAmount(),
				transaction.getFee(),
				transaction.getTax(),
				transaction.getNetAmount(),
				transaction.getCurrency(),
				transaction.getSource(),
				transaction.getMemo(),
				transaction.getCreatedAt(),
				transaction.getUpdatedAt()
		);
	}
}
