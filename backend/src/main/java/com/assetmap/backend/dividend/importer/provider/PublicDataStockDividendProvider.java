package com.assetmap.backend.dividend.importer.provider;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.securityitem.SecurityItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class PublicDataStockDividendProvider implements StockDividendProvider {

	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;
	private static final int PAGE_SIZE = 100;

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final String serviceKey;
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
		this.serviceKey = serviceKey;
		this.resultType = resultType;
	}

	@Override
	public List<ImportedDividendEvent> fetch(SecurityItem securityItem) {
		if (!StringUtils.hasText(serviceKey)) {
			throw new BusinessException(ErrorCode.COMMON_001);
		}

		List<ImportedDividendEvent> events = new ArrayList<>();
		int pageNo = 1;
		int totalCount = Integer.MAX_VALUE;

		while ((pageNo - 1) * PAGE_SIZE < totalCount) {
			JsonNode body = requestPage(securityItem.getName(), pageNo);
			totalCount = body.path("totalCount").asInt(events.size());
			JsonNode items = body.path("items").path("item");
			if (items.isMissingNode() || items.isNull()) {
				break;
			}
			if (items.isArray()) {
				items.forEach(item -> toImportedEvent(item).ifPresent(events::add));
			} else {
				toImportedEvent(items).ifPresent(events::add);
			}
			pageNo++;
		}

		return events;
	}

	private JsonNode requestPage(String securityName, int pageNo) {
		String url = baseUrl + "/getDiviInfo_V2"
				+ "?serviceKey=" + serviceKeyParam()
				+ "&numOfRows=" + PAGE_SIZE
				+ "&pageNo=" + pageNo
				+ "&resultType=" + encode(resultType)
				+ "&stckIssuCmpyNm=" + encode(securityName);

		try {
			String response = restClient.get()
					.uri(url)
					.retrieve()
					.body(String.class);
			JsonNode root = objectMapper.readTree(response);
			return root.path("response").path("body");
		} catch (BusinessException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BusinessException(ErrorCode.COMMON_003, exception);
		}
	}

	private java.util.Optional<ImportedDividendEvent> toImportedEvent(JsonNode item) {
		LocalDate recordDate = parseDate(text(item, "dvdnBasDt"));
		BigDecimal generalDividend = parseAmount(text(item, "stckGenrDvdnAmt"));
		BigDecimal preferredDividend = parseAmount(text(item, "stckGrdnDvdnAmt"));
		BigDecimal dividendPerShare = generalDividend.compareTo(BigDecimal.ZERO) > 0 ? generalDividend : preferredDividend;

		return java.util.Optional.of(new ImportedDividendEvent(
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

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private String serviceKeyParam() {
		return serviceKey.contains("%") ? serviceKey : encode(serviceKey);
	}
}
