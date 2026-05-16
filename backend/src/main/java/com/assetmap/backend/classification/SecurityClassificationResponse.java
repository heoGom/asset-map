package com.assetmap.backend.classification;

import java.time.LocalDateTime;

public record SecurityClassificationResponse(
		Long id,
		Long securityItemId,
		String ticker,
		String securityName,
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
		Boolean hedged,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static SecurityClassificationResponse from(SecurityClassification classification) {
		return new SecurityClassificationResponse(
				classification.getId(),
				classification.getSecurityItem().getId(),
				classification.getSecurityItem().getTicker(),
				classification.getSecurityItem().getName(),
				classification.getCountryGroup(),
				classification.getAssetGroup(),
				classification.getSector(),
				classification.getStrategyType(),
				classification.getTheme(),
				classification.getListingCountry(),
				classification.getExposureCountry(),
				classification.getExposureRegion(),
				classification.getTradingCurrency(),
				classification.getCurrencyExposure(),
				classification.getUnderlyingIndex(),
				classification.getHedged(),
				classification.getCreatedAt(),
				classification.getUpdatedAt()
		);
	}
}
