package com.assetmap.backend.dividend.dashboard.dto;

import java.math.BigDecimal;

public record DividendGrowthResponse(
		int year,
		BigDecimal annualDividendPerShare,
		BigDecimal growthRate
) {
}
