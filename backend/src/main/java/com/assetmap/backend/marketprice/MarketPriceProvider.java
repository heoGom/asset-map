package com.assetmap.backend.marketprice;

import com.assetmap.backend.securityitem.SecurityItem;
import java.time.LocalDate;

public interface MarketPriceProvider {

	MarketPrice fetch(SecurityItem securityItem, LocalDate priceDate);
}
