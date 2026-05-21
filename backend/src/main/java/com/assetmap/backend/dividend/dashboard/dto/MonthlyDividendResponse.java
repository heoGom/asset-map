package com.assetmap.backend.dividend.dashboard.dto;

import java.math.BigDecimal;

public record MonthlyDividendResponse(
		int month,
		BigDecimal amountKrw
) {
}
