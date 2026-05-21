package com.assetmap.backend.dividend.dashboard.dto;

import java.math.BigDecimal;

public record YearlyDividendResponse(
		int year,
		BigDecimal amountKrw
) {
}
