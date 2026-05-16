package com.assetmap.backend.dashboard;

import java.math.BigDecimal;

public record AssetRatioResponse(
		String category,
		BigDecimal amount,
		BigDecimal ratio
) {
}
