package com.assetmap.backend.dividend;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DividendEventUpdateRequest(
		Long securityItemId,
		Integer dividendYear,
		LocalDate exDividendDate,
		LocalDate paymentDate,
		@PositiveOrZero BigDecimal dividendPerShare,
		@Pattern(regexp = ".*\\S.*") String currency,
		DataSourceType source
) {
}
