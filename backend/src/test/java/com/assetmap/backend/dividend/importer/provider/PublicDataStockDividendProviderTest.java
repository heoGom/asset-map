package com.assetmap.backend.dividend.importer.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetmap.backend.dividend.importer.dto.StockDividendFetchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PublicDataStockDividendProviderTest {

	private HttpServer server;
	private final List<String> rawQueries = new ArrayList<>();
	private final List<Map<String, String>> decodedQueries = new ArrayList<>();

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void acceptsEncodedServiceKeyWithoutDoubleEncoding() throws IOException {
		startServer();
		String decodedKey = "abc/def==";
		String encodedKey = URLEncoder.encode(decodedKey, StandardCharsets.UTF_8);
		PublicDataStockDividendProvider provider = provider(encodedKey);

		StockDividendFetchResult result = provider.fetch("삼성전자");

		assertThat(result.itemCount()).isEqualTo(1);
		assertThat(decodedQueries.get(0).get("serviceKey")).isEqualTo(decodedKey);
		assertThat(rawQueries.get(0)).doesNotContain("%252F").doesNotContain("%253D");
	}

	@Test
	void acceptsDecodedServiceKey() throws IOException {
		startServer();
		String decodedKey = "abc/def==";
		PublicDataStockDividendProvider provider = provider(decodedKey);

		provider.fetch("삼성전자");

		assertThat(decodedQueries.get(0).get("serviceKey")).isEqualTo(decodedKey);
		assertThat(rawQueries.get(0)).doesNotContain("%252F").doesNotContain("%253D");
	}

	private PublicDataStockDividendProvider provider(String serviceKey) {
		return new PublicDataStockDividendProvider(new ObjectMapper(), "http://localhost:" + server.getAddress().getPort(), serviceKey, "json");
	}

	private void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/getDiviInfo_V2", this::handle);
		server.start();
	}

	private void handle(HttpExchange exchange) throws IOException {
		rawQueries.add(exchange.getRequestURI().getRawQuery());
		decodedQueries.add(decodeQuery(exchange.getRequestURI().getRawQuery()));
		byte[] response = """
				{"response":{"header":{"resultCode":"00","resultMsg":"NORMAL_SERVICE"},"body":{"totalCount":1,"items":{"item":[{"isinCd":"KR7005931001","isinCdNm":"삼성전자","stckIssuCmpyNm":"삼성전자","scrsItmsKcdNm":"보통주","stckDvdnRcdNm":"현금배당","stckGenrDvdnAmt":"361","dvdnBasDt":"20201231","cashDvdnPayDt":"20210416"}]}}}}
				""".getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, response.length);
		try (OutputStream outputStream = exchange.getResponseBody()) {
			outputStream.write(response);
		}
	}

	private Map<String, String> decodeQuery(String rawQuery) {
		Map<String, String> params = new HashMap<>();
		for (String pair : rawQuery.split("&")) {
			String[] parts = pair.split("=", 2);
			String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
			String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
			params.put(key, value);
		}
		return params;
	}
}
