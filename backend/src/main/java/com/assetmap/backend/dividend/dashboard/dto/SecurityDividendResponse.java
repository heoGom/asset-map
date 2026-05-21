package com.assetmap.backend.dividend.dashboard.dto;

import java.math.BigDecimal;

public record SecurityDividendResponse(
		Long securityItemId,
		String ticker,
		String securityName,
		BigDecimal expectedAnnualDividendKrw,
		BigDecimal receivedDividendKrw,
		BigDecimal dividendYield,
		BigDecimal yieldOnCost,
		BigDecimal contributionRatio
) {
}
