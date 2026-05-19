package com.assetmap.backend.datasync.provider;

import java.time.LocalDate;
import java.util.List;

public interface MarketPriceProvider {

	default List<ImportedMarketPrice> fetchKospiPrices(LocalDate priceDate) {
		return fetchKospiPrices(priceDate, List.of());
	}

	default List<ImportedMarketPrice> fetchKosdaqPrices(LocalDate priceDate) {
		return fetchKosdaqPrices(priceDate, List.of());
	}

	default List<ImportedMarketPrice> fetchEtfPrices(LocalDate priceDate) {
		return fetchEtfPrices(priceDate, List.of());
	}

	List<ImportedMarketPrice> fetchKospiPrices(LocalDate priceDate, List<String> targetTickers);

	List<ImportedMarketPrice> fetchKosdaqPrices(LocalDate priceDate, List<String> targetTickers);

	List<ImportedMarketPrice> fetchEtfPrices(LocalDate priceDate, List<String> targetTickers);
}
