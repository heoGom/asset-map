package com.assetmap.backend.datasync.provider;

import com.assetmap.backend.marketprice.MarketDataSource;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ImportedMarketPrice(
		String ticker,
		LocalDate priceDate,
		BigDecimal closePrice,
		BigDecimal currentPrice,
		BigDecimal changeAmount,
		BigDecimal changeRate,
		BigDecimal openPrice,
		BigDecimal highPrice,
		BigDecimal lowPrice,
		Long volume,
		BigDecimal tradingValue,
		BigDecimal marketCap,
		BigDecimal nav,
		String underlyingIndexName,
		MarketDataSource source
) {
}
