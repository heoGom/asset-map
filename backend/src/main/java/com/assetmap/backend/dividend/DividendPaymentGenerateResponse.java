package com.assetmap.backend.dividend;

import java.math.BigDecimal;
import java.util.List;

public record DividendPaymentGenerateResponse(
		Long dividendEventId,
		int generatedCount,
		BigDecimal totalGrossAmount,
		BigDecimal totalNetAmount,
		List<DividendPaymentResponse> payments
) {
}
