package com.assetmap.backend.dashboard;

import java.math.BigDecimal;

public record AccountAssetRatioResponse(
		Long accountId,
		String accountName,
		BigDecimal amount,
		BigDecimal ratio
) {
}
