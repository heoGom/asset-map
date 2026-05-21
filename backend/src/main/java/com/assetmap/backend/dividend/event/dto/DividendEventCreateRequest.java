package com.assetmap.backend.dividend.event.dto;
import com.assetmap.backend.dividend.event.enums.DividendEventType;
import com.assetmap.backend.dividend.common.DataSourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DividendEventCreateRequest(
		@NotNull Long securityItemId,
		@NotNull Integer dividendYear,
		LocalDate declarationDate,
		LocalDate exDividendDate,
		@NotNull LocalDate recordDate,
		LocalDate paymentDate,
		DividendEventType eventType,
		@NotNull @PositiveOrZero BigDecimal dividendPerShare,
		@NotBlank String currency,
		@NotNull DataSourceType source
) {
}
