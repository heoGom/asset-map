package com.assetmap.backend.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TradeTransactionCreateRequest(
		@NotNull Long userId,
		@NotNull Long accountId,
		@NotNull Long securityItemId,
		@NotNull LocalDate tradeDate,
		@NotNull TradeType tradeType,
		@NotNull @Positive BigDecimal quantity,
		@NotNull @PositiveOrZero BigDecimal price,
		@PositiveOrZero BigDecimal fee,
		@PositiveOrZero BigDecimal tax,
		@NotBlank String currency,
		@NotNull TransactionSource source,
		String memo
) {
}
