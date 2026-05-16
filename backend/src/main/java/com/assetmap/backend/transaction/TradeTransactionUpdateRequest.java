package com.assetmap.backend.transaction;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TradeTransactionUpdateRequest(
		Long userId,
		Long accountId,
		Long securityItemId,
		LocalDate tradeDate,
		TradeType tradeType,
		@Positive BigDecimal quantity,
		@PositiveOrZero BigDecimal price,
		@PositiveOrZero BigDecimal fee,
		@PositiveOrZero BigDecimal tax,
		@Pattern(regexp = ".*\\S.*") String currency,
		TransactionSource source,
		String memo
) {
}
