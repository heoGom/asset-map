package com.assetmap.backend.dividend.importer.provider;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.dividend.importer.dto.StockDividendFetchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class PublicDataStockDividendProvider implements StockDividendProvider {

	private static final Logger log = LoggerFactory.getLogger(PublicDataStockDividendProvider.class);
	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;
	private static final int PAGE_SIZE = 100;

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final String serviceKey;
	private final boolean serviceKeyHasPercent;
	private final String resultType;

	public PublicDataStockDividendProvider(
			ObjectMapper objectMapper,
			@Value("${external.public-data.stock-dividend.base-url}") String baseUrl,
			@Value("${external.public-data.stock-dividend.service-key:}") String serviceKey,
			@Value("${external.public-data.stock-dividend.result-type:json}") String resultType
	) {
		this.restClient = RestClient.create();
		this.objectMapper = objectMapper;
		this.baseUrl = baseUrl;
		this.serviceKeyHasPercent = hasPercentEncoding(serviceKey);
		this.serviceKey = normalizeServiceKey(serviceKey);
		this.resultType = resultType;
	}

	@Override
	public StockDividendFetchResult fetch(String searchTerm) {
		if (!StringUtils.hasText(serviceKey)) {
			log.warn("Stock dividend API key is missing. key_present=false key_has_percent={}", serviceKeyHasPercent);
			throw new BusinessException(ErrorCode.CONFIG_ERROR);
		}

		log.info("Stock dividend API key loaded. key_present=true key_has_percent={}", serviceKeyHasPercent);

		List<ImportedDividendEvent> events = new ArrayList<>();
		int pageNo = 1;
		int totalCount = Integer.MAX_VALUE;
		int httpStatus = 0;
		String resultCode = "";
		String resultMsg = "";

		while ((pageNo - 1) * PAGE_SIZE < totalCount) {
			PageResponse page = requestPage(searchTerm, pageNo);
			httpStatus = page.httpStatus();
			resultCode = page.resultCode();
			resultMsg = page.resultMsg();
			JsonNode body = page.body();
			totalCount = body.path("totalCount").asInt(events.size());
			JsonNode items = body.path("items").path("item");
			if (items.isMissingNode() || items.isNull()) {
				break;
			}
			if (items.isArray()) {
				items.forEach(item -> toImportedEvent(searchTerm, item).ifPresent(events::add));
			} else {
				toImportedEvent(searchTerm, items).ifPresent(events::add);
			}
			pageNo++;
		}

		return new StockDividendFetchResult(searchTerm, httpStatus, resultCode, resultMsg, totalCount == Integer.MAX_VALUE ? 0 : totalCount, events);
	}

	private PageResponse requestPage(String searchTerm, int pageNo) {
		URI uri = UriComponentsBuilder.fromUriString(baseUrl)
				.path("/getDiviInfo_V2")
				.queryParam("serviceKey", serviceKey)
				.queryParam("numOfRows", PAGE_SIZE)
				.queryParam("pageNo", pageNo)
				.queryParam("resultType", resultType)
				.queryParam("stckIssuCmpyNm", searchTerm)
				.encode()
				.build()
				.toUri();
		log.info("Calling stock dividend API. searchTerm={} pageNo={} key_present=true key_has_percent={}", searchTerm, pageNo, serviceKeyHasPercent);

		try {
			ResponseEntity<String> responseEntity = restClient.get()
					.uri(uri)
					.retrieve()
					.toEntity(String.class);
			String response = responseEntity.getBody();
			JsonNode root = objectMapper.readTree(response);
			JsonNode header = root.path("response").path("header");
			JsonNode body = root.path("response").path("body");
			JsonNode items = body.path("items").path("item");
			log.info("Stock dividend API response. httpStatus={} resultCode={} resultMsg={} totalCount={} itemsCount={}",
					responseEntity.getStatusCode().value(),
					header.path("resultCode").asText(""),
					header.path("resultMsg").asText(""),
					body.path("totalCount").asText(""),
					itemCount(items));
			return new PageResponse(
					responseEntity.getStatusCode().value(),
					header.path("resultCode").asText(""),
					header.path("resultMsg").asText(""),
					body
			);
		} catch (BusinessException exception) {
			throw exception;
		} catch (HttpClientErrorException.Unauthorized exception) {
			log.warn("Stock dividend API authentication failure. httpStatus=401 key_present=true key_has_percent={}", serviceKeyHasPercent);
			throw new BusinessException(ErrorCode.API_AUTH_ERROR, exception);
		} catch (RestClientResponseException exception) {
			log.warn("Stock dividend API HTTP failure. httpStatus={} response={}",
					exception.getStatusCode().value(),
					truncate(exception.getResponseBodyAsString()));
			throw new BusinessException(ErrorCode.COMMON_003, exception);
		} catch (Exception exception) {
			log.warn("Stock dividend API parsing/call failure. reason={}", exception.toString());
			throw new BusinessException(ErrorCode.COMMON_003, exception);
		}
	}

	private java.util.Optional<ImportedDividendEvent> toImportedEvent(String searchTerm, JsonNode item) {
		LocalDate recordDate = parseDate(text(item, "dvdnBasDt"));
		BigDecimal dividendPerShare = parseAmount(text(item, "stckGenrDvdnAmt"));

		return java.util.Optional.of(new ImportedDividendEvent(
				searchTerm,
				text(item, "isinCd"),
				text(item, "isinCdNm"),
				text(item, "stckIssuCmpyNm"),
				text(item, "scrsItmsKcdNm"),
				text(item, "stckDvdnRcdNm"),
				recordDate,
				parseDate(text(item, "cashDvdnPayDt")),
				dividendPerShare
		));
	}

	private String text(JsonNode node, String fieldName) {
		String value = node.path(fieldName).asText("");
		return value == null ? "" : value.trim();
	}

	private LocalDate parseDate(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		try {
			return LocalDate.parse(value.trim(), COMPACT_DATE);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}

	private BigDecimal parseAmount(String value) {
		if (!StringUtils.hasText(value)) {
			return BigDecimal.ZERO;
		}
		String normalized = value.replace(",", "").trim();
		try {
			return new BigDecimal(normalized);
		} catch (NumberFormatException exception) {
			return BigDecimal.ZERO;
		}
	}

	private boolean hasPercentEncoding(String value) {
		return StringUtils.hasText(value) && value.matches(".*%[0-9a-fA-F]{2}.*");
	}

	private String normalizeServiceKey(String value) {
		if (!StringUtils.hasText(value)) {
			return "";
		}
		String trimmed = value.trim();
		if (!hasPercentEncoding(trimmed)) {
			return trimmed;
		}
		try {
			return URLDecoder.decode(trimmed, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException exception) {
			return trimmed;
		}
	}

	private int itemCount(JsonNode items) {
		if (items.isArray()) {
			return items.size();
		}
		if (items.isObject()) {
			return 1;
		}
		return 0;
	}

	private String truncate(String value) {
		if (value == null) {
			return "";
		}
		return value.length() <= 300 ? value : value.substring(0, 300);
	}

	private record PageResponse(
			int httpStatus,
			String resultCode,
			String resultMsg,
			JsonNode body
	) {
	}
}
