package com.assetmap.backend.datasync.provider;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class KrxApiClient {

	private static final Logger log = LoggerFactory.getLogger(KrxApiClient.class);
	private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

	private final ObjectMapper objectMapper;
	private final RestClient restClient;
	private final String apiKey;

	@Autowired
	public KrxApiClient(ObjectMapper objectMapper, @Value("${external.krx.api-key:}") String apiKey) {
		this(objectMapper, RestClient.create(), apiKey);
	}

	KrxApiClient(ObjectMapper objectMapper, RestClient restClient, String apiKey) {
		this.objectMapper = objectMapper;
		this.restClient = restClient;
		this.apiKey = apiKey == null ? "" : apiKey.trim();
	}

	public List<JsonNode> postForOutBlockItems(String endpointName, String url, LocalDate basDd) {
		if (!StringUtils.hasText(apiKey)) {
			log.warn("KRX API key is missing. endpoint={} key_present=false", endpointName);
			throw new BusinessException(ErrorCode.CONFIG_ERROR, "CONFIG_ERROR: KRX_API_KEY is missing.");
		}
		String compactBasDd = basDd.format(COMPACT_DATE);
		log.info("Calling KRX API. endpoint={} basDd={} key_present=true", endpointName, compactBasDd);

		try {
			ResponseEntity<String> response = restClient.post()
					.uri(url)
					.header("AUTH_KEY", apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new KrxRequest(compactBasDd))
					.retrieve()
					.toEntity(String.class);
			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode outBlock = root.path("OutBlock_1");
			if (!outBlock.isArray()) {
				throw new BusinessException(ErrorCode.API_RESPONSE_ERROR, "API_RESPONSE_ERROR: KRX response does not contain OutBlock_1 array.");
			}
			List<JsonNode> items = new ArrayList<>();
			outBlock.forEach(items::add);
			log.info("KRX API response parsed. endpoint={} basDd={} itemCount={}", endpointName, compactBasDd, items.size());
			return items;
		} catch (BusinessException exception) {
			throw exception;
		} catch (HttpClientErrorException.Unauthorized exception) {
			log.warn("KRX API authentication failure. endpoint={} httpStatus=401 key_present=true", endpointName);
			throw new BusinessException(ErrorCode.API_AUTH_ERROR, "API_AUTH_ERROR: KRX authentication failed.", exception);
		} catch (RestClientResponseException exception) {
			log.warn("KRX API HTTP failure. endpoint={} httpStatus={}", endpointName, exception.getStatusCode().value());
			throw new BusinessException(ErrorCode.COMMON_003, "KRX API HTTP failure: status=" + exception.getStatusCode().value(), exception);
		} catch (Exception exception) {
			log.warn("KRX API response parsing/call failure. endpoint={} reason={}", endpointName, exception.toString());
			throw new BusinessException(ErrorCode.API_RESPONSE_ERROR, "API_RESPONSE_ERROR: KRX response parsing failed.", exception);
		}
	}

	private record KrxRequest(String basDd) {
	}
}
