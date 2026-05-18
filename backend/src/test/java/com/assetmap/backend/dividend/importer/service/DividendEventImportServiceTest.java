package com.assetmap.backend.dividend.importer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.account.AccountRepository;
import com.assetmap.backend.account.AccountType;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.dividend.DividendPaymentRepository;
import com.assetmap.backend.dividend.importer.dto.DividendImportRequest;
import com.assetmap.backend.dividend.importer.dto.DividendImportResult;
import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.dividend.importer.dto.StockDividendFetchResult;
import com.assetmap.backend.dividend.importer.provider.StockDividendProvider;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemRepository;
import com.assetmap.backend.securityitem.SecurityType;
import com.assetmap.backend.transaction.TradeTransaction;
import com.assetmap.backend.transaction.TradeTransactionRepository;
import com.assetmap.backend.transaction.TradeType;
import com.assetmap.backend.transaction.TransactionSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.sql.init.mode=never",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class DividendEventImportServiceTest {

	private static final Long USER_ID = 1L;

	@Autowired
	private DividendEventImportService importService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private SecurityItemRepository securityItemRepository;
	@Autowired
	private TradeTransactionRepository transactionRepository;
	@Autowired
	private DividendEventRepository eventRepository;
	@Autowired
	private DividendPaymentRepository paymentRepository;
	@MockitoBean
	private StockDividendProvider stockDividendProvider;

	@BeforeEach
	void setUp() {
		paymentRepository.deleteAll();
		eventRepository.deleteAll();
		transactionRepository.deleteAll();
		accountRepository.deleteAll();
		securityItemRepository.deleteAll();
	}

	@Test
	void importsSamsungElectronicsPreferredShareUsingCommonCompanySearchTerm() {
		SecurityItem security = security("005935", "삼성전자우");
		addPosition(security, new BigDecimal("10"));
		when(stockDividendProvider.fetch("삼성전자우")).thenReturn(fetch("삼성전자우"));
		when(stockDividendProvider.fetch("삼성전자")).thenReturn(fetch(
				"삼성전자",
				event("삼성전자", "삼성전자", "보통주", "현금배당", "20201231", "20210416", "361"),
				event("삼성전자", "삼성전자1우", "우선주", "현금배당", "20201231", "20210416", "362")
		));

		DividendImportResult result = importService.importMySecurities(USER_ID, new DividendImportRequest(null, 2020, 2026));

		assertThat(result.targetSecurityCount()).isEqualTo(1);
		assertThat(result.importedEventCount()).isEqualTo(1);
		assertThat(result.generatedPaymentCount()).isEqualTo(1);
		assertThat(eventRepository.findBySecurityItemId(security.getId())).hasSize(1);
		assertThat(paymentRepository.findByUserId(USER_ID)).hasSize(1);
		assertThat(result.securities().get(0).searchTerms()).containsExactly("삼성전자우", "삼성전자");
	}

	@Test
	void importsHyundaiMotorThirdPreferredShareOnlyWhenThirdPreferredResponseMatches() {
		SecurityItem security = security("005389", "현대차3우B");
		addPosition(security, new BigDecimal("5"));
		when(stockDividendProvider.fetch("현대차3우B")).thenReturn(fetch("현대차3우B"));
		when(stockDividendProvider.fetch("현대자동차")).thenReturn(fetch(
				"현대자동차",
				event("현대자동차", "현대자동차2우", "2우선주", "현금배당", "20201231", "20210416", "3000"),
				event("현대자동차", "현대자동차3우", "3우선주", "현금배당", "20201231", "20210416", "3050")
		));

		DividendImportResult result = importService.importMySecurities(USER_ID, new DividendImportRequest(null, 2020, 2026));

		assertThat(result.importedEventCount()).isEqualTo(1);
		assertThat(result.skippedEventCount()).isEqualTo(1);
		assertThat(result.generatedPaymentCount()).isEqualTo(1);
		assertThat(eventRepository.findBySecurityItemId(security.getId()).get(0).getDividendPerShare()).isEqualByComparingTo("3050");
		assertThat(result.securities().get(0).searchTerms()).containsExactly("현대차3우B", "현대자동차");
	}

	@Test
	void importsSamsungFirePreferredShareOnlyFromPreferredResponse() {
		SecurityItem security = security("000815", "삼성화재우");
		addPosition(security, new BigDecimal("2"));
		when(stockDividendProvider.fetch("삼성화재우")).thenReturn(fetch("삼성화재우"));
		when(stockDividendProvider.fetch("삼성화재")).thenReturn(fetch(
				"삼성화재",
				event("삼성화재", "삼성화재", "보통주", "현금배당", "20201231", "20210416", "8800"),
				event("삼성화재", "삼성화재우", "우선주", "현금배당", "20201231", "20210416", "8805")
		));

		DividendImportResult result = importService.importMySecurities(USER_ID, new DividendImportRequest(null, 2020, 2026));

		assertThat(result.importedEventCount()).isEqualTo(1);
		assertThat(result.generatedPaymentCount()).isEqualTo(1);
		assertThat(eventRepository.findBySecurityItemId(security.getId()).get(0).getDividendPerShare()).isEqualByComparingTo("8805");
		assertThat(result.securities().get(0).searchTerms()).containsExactly("삼성화재우", "삼성화재");
	}

	@Test
	void importsKolonAfter2020AndSkipsZeroNoDividendThenPreventsDuplicateImport() {
		SecurityItem security = security("002020", "코오롱");
		addPosition(security, new BigDecimal("3"));
		when(stockDividendProvider.fetch("코오롱")).thenReturn(fetch(
				"코오롱",
				event("코오롱", "코오롱", "보통주", "현금배당", "20191231", "20200416", "900"),
				event("코오롱", "코오롱", "보통주", "무배당", "20201231", "20210416", "900"),
				event("코오롱", "코오롱", "보통주", "현금배당", "20211231", "20220416", "0"),
				event("코오롱", "코오롱", "보통주", "현금배당", "20221231", "20230416", "1200")
		));

		DividendImportResult first = importService.importMySecurities(USER_ID, new DividendImportRequest(null, 2020, 2026));
		DividendImportResult second = importService.importMySecurities(USER_ID, new DividendImportRequest(null, 2020, 2026));

		assertThat(first.importedEventCount()).isEqualTo(1);
		assertThat(first.skippedEventCount()).isEqualTo(3);
		assertThat(first.generatedPaymentCount()).isEqualTo(1);
		assertThat(second.importedEventCount()).isZero();
		assertThat(second.skippedEventCount()).isEqualTo(4);
		assertThat(eventRepository.findBySecurityItemId(security.getId())).hasSize(1);
		assertThat(paymentRepository.findByUserId(USER_ID)).hasSize(1);
	}

	private SecurityItem security(String ticker, String name) {
		return securityItemRepository.save(new SecurityItem(ticker, name, "KOSPI", "KR", "KRW", SecurityType.STOCK));
	}

	private void addPosition(SecurityItem securityItem, BigDecimal quantity) {
		Account account = accountRepository.save(new Account(USER_ID, "테스트계좌", "TEST", AccountType.GENERAL, "KRW", null));
		transactionRepository.save(new TradeTransaction(
				USER_ID,
				account,
				securityItem,
				LocalDate.of(2019, 1, 1),
				TradeType.INITIAL,
				quantity,
				BigDecimal.ONE,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				"KRW",
				TransactionSource.INITIAL,
				null
		));
	}

	private StockDividendFetchResult fetch(String searchTerm, ImportedDividendEvent... items) {
		return new StockDividendFetchResult(searchTerm, 200, "00", "NORMAL_SERVICE", items.length, List.of(items));
	}

	private ImportedDividendEvent event(
			String companyName,
			String isinName,
			String stockTypeName,
			String dividendRecordName,
			String recordDate,
			String paymentDate,
			String dividendPerShare
	) {
		return new ImportedDividendEvent(
				"",
				"KR7000000000",
				isinName,
				companyName,
				stockTypeName,
				dividendRecordName,
				LocalDate.parse(recordDate, java.time.format.DateTimeFormatter.BASIC_ISO_DATE),
				LocalDate.parse(paymentDate, java.time.format.DateTimeFormatter.BASIC_ISO_DATE),
				new BigDecimal(dividendPerShare)
		);
	}
}
