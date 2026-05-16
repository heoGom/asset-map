package com.assetmap.backend.securityitem;

import jakarta.validation.constraints.Pattern;

public record SecurityItemUpdateRequest(
		@Pattern(regexp = ".*\\S.*") String ticker,
		@Pattern(regexp = ".*\\S.*") String name,
		String market,
		String country,
		@Pattern(regexp = ".*\\S.*") String currency,
		SecurityType securityType
) {
}
