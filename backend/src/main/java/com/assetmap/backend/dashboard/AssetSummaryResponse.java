package com.assetmap.backend.dashboard;

import java.math.BigDecimal;

public record AssetSummaryResponse(
		BigDecimal totalInvestedAmount,
		BigDecimal totalEvaluatedAmount,
		BigDecimal totalProfitLoss,
		BigDecimal totalProfitLossRate,
		long holdingCount
) {
}
