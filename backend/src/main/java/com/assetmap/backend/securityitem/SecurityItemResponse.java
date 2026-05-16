package com.assetmap.backend.securityitem;

import java.time.LocalDateTime;

public record SecurityItemResponse(
		Long id,
		String ticker,
		String name,
		String market,
		String country,
		String currency,
		SecurityType securityType,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static SecurityItemResponse from(SecurityItem securityItem) {
		return new SecurityItemResponse(
				securityItem.getId(),
				securityItem.getTicker(),
				securityItem.getName(),
				securityItem.getMarket(),
				securityItem.getCountry(),
				securityItem.getCurrency(),
				securityItem.getSecurityType(),
				securityItem.getCreatedAt(),
				securityItem.getUpdatedAt()
		);
	}
}
