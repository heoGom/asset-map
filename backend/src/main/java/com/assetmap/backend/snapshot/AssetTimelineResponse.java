package com.assetmap.backend.snapshot;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetTimelineResponse(
		LocalDate date,
		BigDecimal totalAssetAmount
) {
}
