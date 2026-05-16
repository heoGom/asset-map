package com.assetmap.backend.dividend;

import java.math.BigDecimal;

public record MonthlyDividendResponse(
		int month,
		BigDecimal amountKrw
) {
}
