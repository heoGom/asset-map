package com.assetmap.backend.datasync.provider;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubKrxMarketPriceProvider implements MarketPriceProvider {

	private static final Logger log = LoggerFactory.getLogger(StubKrxMarketPriceProvider.class);

	@Override
	public List<ImportedMarketPrice> fetchKospiPrices(LocalDate priceDate, List<String> targetTickers) {
		log.info("KRX market price provider is not implemented yet. market=KOSPI priceDate={}", priceDate);
		return List.of();
	}

	@Override
	public List<ImportedMarketPrice> fetchKosdaqPrices(LocalDate priceDate, List<String> targetTickers) {
		log.info("KRX market price provider is not implemented yet. market=KOSDAQ priceDate={}", priceDate);
		return List.of();
	}

	@Override
	public List<ImportedMarketPrice> fetchEtfPrices(LocalDate priceDate, List<String> targetTickers) {
		log.info("KRX market price provider is not implemented yet. market=ETF priceDate={}", priceDate);
		return List.of();
	}
}
