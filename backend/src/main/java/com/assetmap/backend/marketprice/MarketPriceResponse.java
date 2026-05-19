package com.assetmap.backend.marketprice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MarketPriceResponse(
		Long id,
		Long securityItemId,
		String ticker,
		String securityName,
		LocalDate priceDate,
		BigDecimal closePrice,
		BigDecimal currentPrice,
		BigDecimal changeAmount,
		BigDecimal changeRate,
		Long volume,
		BigDecimal openPrice,
		BigDecimal highPrice,
		BigDecimal lowPrice,
		BigDecimal tradingValue,
		BigDecimal marketCap,
		BigDecimal nav,
		String underlyingIndexName,
		MarketDataSource source,
		LocalDateTime fetchedAt,
		LocalDateTime createdAt
) {

	public static MarketPriceResponse from(MarketPrice marketPrice) {
		return new MarketPriceResponse(
				marketPrice.getId(),
				marketPrice.getSecurityItem().getId(),
				marketPrice.getSecurityItem().getTicker(),
				marketPrice.getSecurityItem().getName(),
				marketPrice.getPriceDate(),
				marketPrice.getClosePrice(),
				marketPrice.getCurrentPrice(),
				marketPrice.getChangeAmount(),
				marketPrice.getChangeRate(),
				marketPrice.getVolume(),
				marketPrice.getOpenPrice(),
				marketPrice.getHighPrice(),
				marketPrice.getLowPrice(),
				marketPrice.getTradingValue(),
				marketPrice.getMarketCap(),
				marketPrice.getNav(),
				marketPrice.getUnderlyingIndexName(),
				marketPrice.getSource(),
				marketPrice.getFetchedAt(),
				marketPrice.getCreatedAt()
		);
	}
}
