package com.assetmap.backend.holding;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record HoldingUpdateRequest(
		Long userId,
		Long accountId,
		Long securityItemId,
		@PositiveOrZero BigDecimal quantity,
		@PositiveOrZero BigDecimal averagePrice,
		@PositiveOrZero BigDecimal currentPrice,
		@Pattern(regexp = ".*\\S.*") String currency
) {
}
