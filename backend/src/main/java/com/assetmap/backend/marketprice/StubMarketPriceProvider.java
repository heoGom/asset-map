package com.assetmap.backend.marketprice;

import com.assetmap.backend.securityitem.SecurityItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class StubMarketPriceProvider implements MarketPriceProvider {

	@Override
	public MarketPrice fetch(SecurityItem securityItem, LocalDate priceDate) {
		LocalDate targetDate = priceDate == null ? LocalDate.now() : priceDate;
		return new MarketPrice(
				securityItem,
				targetDate,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				0L,
				MarketDataSource.PUBLIC_DATA,
				LocalDateTime.now()
		);
	}
}
