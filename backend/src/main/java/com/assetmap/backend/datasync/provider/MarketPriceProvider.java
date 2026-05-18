package com.assetmap.backend.datasync.provider;

import java.time.LocalDate;
import java.util.List;

public interface MarketPriceProvider {

	List<ImportedMarketPrice> fetchKospiPrices(LocalDate priceDate);

	List<ImportedMarketPrice> fetchKosdaqPrices(LocalDate priceDate);

	List<ImportedMarketPrice> fetchEtfPrices(LocalDate priceDate);
}
