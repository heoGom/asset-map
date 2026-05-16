package com.assetmap.backend.marketprice;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MarketPriceRefreshRequest(
		@NotNull Long securityItemId,
		LocalDate priceDate
) {
}
