package com.assetmap.backend.classification;

import jakarta.validation.constraints.NotNull;

public record SecurityClassificationCreateRequest(
		@NotNull Long securityItemId,
		@NotNull CountryGroup countryGroup,
		@NotNull AssetGroup assetGroup,
		@NotNull Sector sector,
		@NotNull StrategyType strategyType,
		String theme,
		String listingCountry,
		String exposureCountry,
		String exposureRegion,
		String tradingCurrency,
		String currencyExposure,
		String underlyingIndex,
		Boolean hedged
) {
}
