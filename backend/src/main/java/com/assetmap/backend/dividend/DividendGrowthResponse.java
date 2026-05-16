package com.assetmap.backend.dividend;

import java.math.BigDecimal;

public record DividendGrowthResponse(
		int year,
		BigDecimal annualDividendPerShare,
		BigDecimal growthRate
) {
}
