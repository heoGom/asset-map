package com.assetmap.backend.dividend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DividendPaymentCreateRequest(
		@NotNull Long userId,
		@NotNull Long accountId,
		@NotNull Long securityItemId,
		Long dividendEventId,
		@NotNull @PositiveOrZero BigDecimal quantityAtRecordDate,
		@NotNull @PositiveOrZero BigDecimal dividendPerShare,
		@PositiveOrZero BigDecimal taxAmount,
		@NotBlank String currency,
		@PositiveOrZero BigDecimal exchangeRate,
		LocalDate paymentDate,
		@NotNull DividendPaymentStatus status
) {
}
