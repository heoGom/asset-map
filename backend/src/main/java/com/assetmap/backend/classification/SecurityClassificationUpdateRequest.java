package com.assetmap.backend.classification;

public record SecurityClassificationUpdateRequest(
		CountryGroup countryGroup,
		AssetGroup assetGroup,
		Sector sector,
		StrategyType strategyType,
		String theme
) {
}
