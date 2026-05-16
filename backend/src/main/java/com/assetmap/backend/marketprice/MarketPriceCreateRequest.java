package com.assetmap.backend.marketprice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketPriceCreateRequest(
		@NotNull Long securityItemId,
		@NotNull LocalDate priceDate,
		@NotNull @PositiveOrZero BigDecimal closePrice,
		@NotNull @PositiveOrZero BigDecimal currentPrice,
		BigDecimal changeAmount,
		BigDecimal changeRate,
		@PositiveOrZero Long volume,
		@NotNull MarketDataSource source
) {
}
