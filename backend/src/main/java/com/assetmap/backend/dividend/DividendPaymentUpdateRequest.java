package com.assetmap.backend.dividend;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DividendPaymentUpdateRequest(
		Long userId,
		Long accountId,
		Long securityItemId,
		Long dividendEventId,
		@PositiveOrZero BigDecimal quantityAtRecordDate,
		@PositiveOrZero BigDecimal dividendPerShare,
		@PositiveOrZero BigDecimal taxAmount,
		@Pattern(regexp = ".*\\S.*") String currency,
		@PositiveOrZero BigDecimal exchangeRate,
		LocalDate paymentDate,
		DividendPaymentStatus status
) {
}
