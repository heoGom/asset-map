package com.assetmap.backend.dividend;

import java.math.BigDecimal;

public record YearlyDividendResponse(
		int year,
		BigDecimal amountKrw
) {
}
