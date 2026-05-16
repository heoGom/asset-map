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
				classification.getCreatedAt(),
				classification.getUpdatedAt()
		);
	}
}
