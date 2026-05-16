package com.assetmap.backend.dividend;

import java.math.BigDecimal;

public record DividendSummaryResponse(
		BigDecimal expectedAnnualDividendKrw,
		BigDecimal averageMonthlyDividendKrw,
		BigDecimal portfolioDividendYield,
		BigDecimal yieldOnCost,
		BigDecimal currentYearReceivedDividendKrw,
		BigDecimal totalReceivedDividendKrw
) {
}
