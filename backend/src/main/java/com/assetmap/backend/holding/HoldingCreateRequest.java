package com.assetmap.backend.holding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record HoldingCreateRequest(
		@NotNull Long userId,
		@NotNull Long accountId,
		@NotNull Long securityItemId,
		@NotNull @PositiveOrZero BigDecimal quantity,
		@NotNull @PositiveOrZero BigDecimal averagePrice,
		@NotNull @PositiveOrZero BigDecimal currentPrice,
		@NotBlank String currency
) {
}
