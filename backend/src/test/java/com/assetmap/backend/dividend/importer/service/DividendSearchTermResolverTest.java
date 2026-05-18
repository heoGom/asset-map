package com.assetmap.backend.dividend.importer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityType;
import org.junit.jupiter.api.Test;

class DividendSearchTermResolverTest {

	private final DividendSearchTermResolver resolver = new DividendSearchTermResolver();

	@Test
	void resolvesPreferredShareBaseNames() {
		assertThat(resolver.resolve(stock("삼성전자우"))).containsExactly("삼성전자우", "삼성전자");
		assertThat(resolver.resolve(stock("삼성화재우"))).containsExactly("삼성화재우", "삼성화재");
	}

	@Test
	void resolvesHyundaiMotorPreferredSharesToOfficialCompanyName() {
		assertThat(resolver.resolve(stock("현대차3우B"))).containsExactly("현대차3우B", "현대자동차");
		assertThat(resolver.resolve(stock("현대차2우B"))).containsExactly("현대차2우B", "현대자동차");
		assertThat(resolver.resolve(stock("현대차우"))).containsExactly("현대차우", "현대자동차");
	}

	@Test
	void keepsCommonShareNameAsSingleSearchTerm() {
		assertThat(resolver.resolve(stock("코오롱"))).containsExactly("코오롱");
	}

	private SecurityItem stock(String name) {
		return new SecurityItem("000000", name, "KOSPI", "KR", "KRW", SecurityType.STOCK);
	}
}
