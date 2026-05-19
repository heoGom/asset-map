package com.assetmap.backend.datasync.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.assetmap.backend.marketprice.MarketDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KrxMarketPriceProviderTest {

	@Test
	void fetchesWholeKrxResponseButReturnsOnlyTargetTickers() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KrxApiClient client = new KrxApiClient(new ObjectMapper(), builder.build(), "test-api-key");
		KrxMarketPriceProvider provider = new KrxMarketPriceProvider(client, "https://krx.test/kospi", "https://krx.test/kosdaq", "https://krx.test/etf");

		server.expect(requestTo("https://krx.test/kospi"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("AUTH_KEY", "test-api-key"))
				.andExpect(content().json("{\"basDd\":\"20260515\"}"))
				.andRespond(withSuccess("""
						{
						  "OutBlock_1": [
						    {
						      "BAS_DD": "20260515",
						      "ISU_CD": "005935",
						      "ISU_NM": "삼성전자우",
						      "TDD_CLSPRC": "55,700",
						      "CMPPREVDD_PRC": "100",
						      "FLUC_RT": "0.18",
						      "TDD_OPNPRC": "55,000",
						      "TDD_HGPRC": "56,000",
						      "TDD_LWPRC": "54,900",
						      "ACC_TRDVOL": "1,234",
						      "ACC_TRDVAL": "68,733,800",
						      "MKTCAP": "45,000,000,000"
						    },
						    {
						      "BAS_DD": "20260515",
						      "ISU_CD": "999999",
						      "TDD_CLSPRC": "1,000"
						    }
						  ]
						}
						""", MediaType.APPLICATION_JSON));

		List<ImportedMarketPrice> prices = provider.fetchKospiPrices(LocalDate.of(2026, 5, 15), List.of("005935"));

		assertThat(prices).hasSize(1);
		assertThat(prices.get(0).ticker()).isEqualTo("005935");
		assertThat(prices.get(0).priceDate()).isEqualTo(LocalDate.of(2026, 5, 15));
		assertThat(prices.get(0).closePrice()).isEqualByComparingTo("55700");
		assertThat(prices.get(0).changeRate()).isEqualByComparingTo("0.18");
		assertThat(prices.get(0).volume()).isEqualTo(1234L);
		assertThat(prices.get(0).marketCap()).isEqualByComparingTo("45000000000");
		assertThat(prices.get(0).source()).isEqualTo(MarketDataSource.KRX);
	}

	@Test
	void mapsEtfNavAndUnderlyingIndexName() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KrxApiClient client = new KrxApiClient(new ObjectMapper(), builder.build(), "test-api-key");
		KrxMarketPriceProvider provider = new KrxMarketPriceProvider(client, "https://krx.test/kospi", "https://krx.test/kosdaq", "https://krx.test/etf");

		server.expect(requestTo("https://krx.test/etf"))
				.andRespond(withSuccess("""
						{
						  "OutBlock_1": [
						    {
						      "BAS_DD": "20260515",
						      "ISU_CD": "133690",
						      "TDD_CLSPRC": "150,000",
						      "NAV": "149,900",
						      "IDX_IND_NM": "NASDAQ 100"
						    }
						  ]
						}
						""", MediaType.APPLICATION_JSON));

		List<ImportedMarketPrice> prices = provider.fetchEtfPrices(LocalDate.of(2026, 5, 15), List.of("133690"));

		assertThat(prices).hasSize(1);
		assertThat(prices.get(0).ticker()).isEqualTo("133690");
		assertThat(prices.get(0).nav()).isEqualByComparingTo("149900");
		assertThat(prices.get(0).underlyingIndexName()).isEqualTo("NASDAQ 100");
	}
}
