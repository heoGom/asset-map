package com.assetmap.backend.datasync.provider;

import com.assetmap.backend.marketprice.MarketDataSource;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KrxMarketPriceProvider implements MarketPriceProvider {

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final KrxApiClient krxApiClient;
	private final String kospiUrl;
	private final String kosdaqUrl;
	private final String etfUrl;

	public KrxMarketPriceProvider(
			KrxApiClient krxApiClient,
			@Value("${external.krx.market-price.kospi-url}") String kospiUrl,
			@Value("${external.krx.market-price.kosdaq-url}") String kosdaqUrl,
			@Value("${external.krx.market-price.etf-url}") String etfUrl
	) {
		this.krxApiClient = krxApiClient;
		this.kospiUrl = kospiUrl;
		this.kosdaqUrl = kosdaqUrl;
		this.etfUrl = etfUrl;
	}

	@Override
	public List<ImportedMarketPrice> fetchKospiPrices(LocalDate priceDate, List<String> targetTickers) {
		return fetch("KOSPI_MARKET_PRICE", kospiUrl, priceDate, targetTickers);
	}

	@Override
	public List<ImportedMarketPrice> fetchKosdaqPrices(LocalDate priceDate, List<String> targetTickers) {
		return fetch("KOSDAQ_MARKET_PRICE", kosdaqUrl, priceDate, targetTickers);
	}

	@Override
	public List<ImportedMarketPrice> fetchEtfPrices(LocalDate priceDate, List<String> targetTickers) {
		return fetch("ETF_MARKET_PRICE", etfUrl, priceDate, targetTickers);
	}

	private List<ImportedMarketPrice> fetch(String endpointName, String url, LocalDate priceDate, List<String> targetTickers) {
		Set<String> targets = new HashSet<>(targetTickers);
		return krxApiClient.postForOutBlockItems(endpointName, url, priceDate)
				.stream()
				.filter(item -> targets.contains(text(item, "ISU_CD")))
				.map(item -> toImportedMarketPrice(item, priceDate))
				.toList();
	}

	private ImportedMarketPrice toImportedMarketPrice(JsonNode item, LocalDate fallbackPriceDate) {
		BigDecimal closePrice = KrxNumberParser.decimal(text(item, "TDD_CLSPRC"));
		return new ImportedMarketPrice(
				text(item, "ISU_CD"),
				parseDate(text(item, "BAS_DD"), fallbackPriceDate),
				closePrice,
				closePrice,
				KrxNumberParser.decimal(text(item, "CMPPREVDD_PRC")),
				KrxNumberParser.decimal(text(item, "FLUC_RT")),
				KrxNumberParser.decimal(text(item, "TDD_OPNPRC")),
				KrxNumberParser.decimal(text(item, "TDD_HGPRC")),
				KrxNumberParser.decimal(text(item, "TDD_LWPRC")),
				KrxNumberParser.longValue(text(item, "ACC_TRDVOL")),
				KrxNumberParser.decimal(text(item, "ACC_TRDVAL")),
				KrxNumberParser.decimal(text(item, "MKTCAP")),
				KrxNumberParser.decimal(text(item, "NAV")),
				text(item, "IDX_IND_NM"),
				MarketDataSource.KRX
		);
	}

	private String text(JsonNode node, String fieldName) {
		String value = node.path(fieldName).asText("");
		if (!StringUtils.hasText(value)) {
			return "";
		}
		String trimmed = value.trim();
		return "-".equals(trimmed) ? "" : trimmed;
	}

	private LocalDate parseDate(String value, LocalDate fallback) {
		if (!StringUtils.hasText(value)) {
			return fallback;
		}
		try {
			return LocalDate.parse(value.trim(), COMPACT_DATE);
		} catch (DateTimeParseException exception) {
			return fallback;
		}
	}
}
