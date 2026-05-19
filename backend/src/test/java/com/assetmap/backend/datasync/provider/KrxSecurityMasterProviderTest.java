package com.assetmap.backend.datasync.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.securityitem.SecurityType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KrxSecurityMasterProviderTest {

	private static final String TEST_KEY = "test-api-key";

	@Test
	void krxApiClientPostsJsonBodyWithAuthHeaderAndParsesOutBlock() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KrxApiClient client = new KrxApiClient(new ObjectMapper(), builder.build(), TEST_KEY);

		server.expect(once(), requestTo("https://krx.test/security-master"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("AUTH_KEY", TEST_KEY))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("{\"basDd\":\"20260515\"}"))
				.andRespond(withSuccess("{\"OutBlock_1\":[{\"ISU_SRT_CD\":\"005935\"}]}", MediaType.APPLICATION_JSON));

		assertThat(client.postForOutBlockItems("TEST", "https://krx.test/security-master", LocalDate.of(2026, 5, 15)))
				.hasSize(1);
		server.verify();
	}

	@Test
	void missingKrxApiKeyThrowsConfigErrorBeforeHttpCall() {
		KrxApiClient client = new KrxApiClient(new ObjectMapper(), RestClient.create(), "");

		assertThatThrownBy(() -> client.postForOutBlockItems("TEST", "https://krx.test/security-master", LocalDate.of(2026, 5, 15)))
				.isInstanceOfSatisfying(BusinessException.class, exception ->
						assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONFIG_ERROR)
				);
	}

	@Test
	void mapsKrxSecurityMasterResponseToImportedSecurityMaster() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KrxApiClient client = new KrxApiClient(new ObjectMapper(), builder.build(), TEST_KEY);
		KrxSecurityMasterProvider provider = new KrxSecurityMasterProvider(client, "https://krx.test/kospi", "https://krx.test/kosdaq");

		server.expect(requestTo("https://krx.test/kospi"))
				.andRespond(withSuccess("""
						{
						  "OutBlock_1": [
						    {
						      "ISU_CD": "KR7005931001",
						      "ISU_SRT_CD": "005935",
						      "ISU_NM": "삼성전자우",
						      "ISU_ABBRV": "삼성전자우",
						      "ISU_ENG_NM": "SamsungElec(1P)",
						      "LIST_DD": "19750611",
						      "MKT_TP_NM": "KOSPI"
						    }
						  ]
						}
						""", MediaType.APPLICATION_JSON));

		List<ImportedSecurityMaster> securities = provider.fetchAllKospi(LocalDate.of(2026, 5, 15));

		assertThat(securities).hasSize(1);
		assertThat(securities.get(0).ticker()).isEqualTo("005935");
		assertThat(securities.get(0).isinCode()).isEqualTo("KR7005931001");
		assertThat(securities.get(0).name()).isEqualTo("삼성전자우");
		assertThat(securities.get(0).market()).isEqualTo("KOSPI");
		assertThat(securities.get(0).securityType()).isEqualTo(SecurityType.STOCK);
		assertThat(securities.get(0).listingDate()).isEqualTo(LocalDate.of(1975, 6, 11));
	}
}
