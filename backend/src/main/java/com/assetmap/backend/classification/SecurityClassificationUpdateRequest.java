package com.assetmap.backend.classification;

public record SecurityClassificationUpdateRequest(
		CountryGroup countryGroup,
		AssetGroup assetGroup,
		Sector sector,
		StrategyType strategyType,
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
