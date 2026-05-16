package com.assetmap.backend.securityitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SecurityItemCreateRequest(
		@NotBlank String ticker,
		@NotBlank String name,
		String market,
		String country,
		@NotBlank String currency,
		@NotNull SecurityType securityType
) {
}
