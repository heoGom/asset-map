package com.assetmap.backend.snapshot;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountTimelineResponse(
		LocalDate date,
		Long accountId,
		String accountName,
		BigDecimal totalAssetAmount
) {
}
