package com.assetmap.backend.dividend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DividendEventCreateRequest(
		@NotNull Long securityItemId,
		@NotNull Integer dividendYear,
		LocalDate exDividendDate,
		LocalDate paymentDate,
		@NotNull @PositiveOrZero BigDecimal dividendPerShare,
		@NotBlank String currency,
		@NotNull DataSourceType source
) {
}
