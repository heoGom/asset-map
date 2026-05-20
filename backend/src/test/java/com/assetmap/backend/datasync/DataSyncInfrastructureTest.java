package com.assetmap.backend.datasync;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.account.AccountRepository;
import com.assetmap.backend.account.AccountType;
import com.assetmap.backend.datasync.provider.MarketPriceProvider;
import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.dividend.DividendPaymentRepository;
import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.dividend.importer.dto.StockDividendFetchResult;
import com.assetmap.backend.dividend.importer.provider.StockDividendProvider;
import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPriceRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class DataSyncInfrastructureTest {

	@Autowired
	private DataSyncStatusService dataSyncStatusService;
	@Autowired
	private DataSyncStatusRepository dataSyncStatusRepository;
	@Autowired
	private AdminSyncService adminSyncService;
	@Autowired
	private SecurityMasterSyncService securityMasterSyncService;
	@Autowired
	private MarketPriceSyncService marketPriceSyncService;
	@Autowired
	private SecurityItemRepository securityItemRepository;
	@Autowired
	private MarketPriceRepository marketPriceRepository;
	@Autowired
	private HoldingRepository holdingRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private TradeTransactionRepository tradeTransactionRepository;
	@Autowired
	private DividendEventRepository dividendEventRepository;
	@Autowired
	private DividendPaymentRepository dividendPaymentRepository;
	@MockitoBean
	private MarketPriceProvider marketPriceProvider;
	@MockitoBean
	private StockDividendProvider stockDividendProvider;

	@BeforeEach
	void setUp() {
		dividendPaymentRepository.deleteAll();
		dividendEventRepository.deleteAll();
		marketPriceRepository.deleteAll();
		holdingRepository.deleteAll();
		tradeTransactionRepository.deleteAll();
		accountRepository.deleteAll();
		securityItemRepository.deleteAll();
		dataSyncStatusRepository.deleteAll();
	}

	@Test
	void dataSyncStatusMarksAndChecksToday() {
		assertThat(dataSyncStatusService.shouldSyncToday(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", false)).isTrue();

		dataSyncStatusService.markRunning(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", "running");
		dataSyncStatusService.markSuccess(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", LocalDate.now(), "done");
		dataSyncStatusService.markSkipped(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", "already synced");

		assertThat(dataSyncStatusService.shouldSyncToday(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", false)).isFalse();
		assertThat(dataSyncStatusService.shouldSyncToday(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", true)).isTrue();
		assertThat(dataSyncStatusService.getStatus(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL").status()).isEqualTo(DataSyncStatusValue.SUCCESS);
	}

	@Test
	void upsertsSecurityMasterByTicker() {
		SyncUpsertResult first = securityMasterSyncService.upsertImportedSecurities(List.of(
				new ImportedSecurityMaster("005930", "KR7005930003", "삼성전자", "삼성전자", "Samsung Electronics", "KOSPI", SecurityType.STOCK, LocalDate.of(1975, 6, 11), "KRW", DataSyncSource.KRX)
		));
		SyncUpsertResult second = securityMasterSyncService.upsertImportedSecurities(List.of(
				new ImportedSecurityMaster("005930", "KR7005930003", "삼성전자보통주", "삼성전자", "Samsung Electronics", "KOSPI", SecurityType.STOCK, LocalDate.of(1975, 6, 11), "KRW", DataSyncSource.KRX)
		));

		assertThat(first.insertedCount()).isEqualTo(1);
		assertThat(second.updatedCount()).isEqualTo(1);
		assertThat(securityItemRepository.findAll()).hasSize(1);
		assertThat(securityItemRepository.findByTicker("005930").orElseThrow().getName()).isEqualTo("삼성전자보통주");
		assertThat(securityItemRepository.findByTicker("005930").orElseThrow().getIsinCode()).isEqualTo("KR7005930003");
	}

	@Test
	void upsertsMarketPriceAndUpdatesHoldingCurrentPriceWhenLatest() {
		SecurityItem securityItem = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		holdingRepository.save(new Holding(1L, account, securityItem, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, "KRW"));

		SyncUpsertResult first = marketPriceSyncService.upsertImportedPrices(List.of(price("005930", LocalDate.of(2026, 5, 15), "70000")));
		SyncUpsertResult second = marketPriceSyncService.upsertImportedPrices(List.of(price("005930", LocalDate.of(2026, 5, 15), "71000")));
		SyncUpsertResult skipped = marketPriceSyncService.upsertImportedPrices(List.of(price("999999", LocalDate.of(2026, 5, 15), "1000")));

		assertThat(first.insertedCount()).isEqualTo(1);
		assertThat(second.updatedCount()).isEqualTo(1);
		assertThat(skipped.skippedCount()).isEqualTo(1);
		assertThat(marketPriceRepository.findAll()).hasSize(1);
		assertThat(marketPriceRepository.findAll().get(0).getCurrentPrice()).isEqualByComparingTo("71000");
		assertThat(holdingRepository.findBySecurityItemId(securityItem.getId()).get(0).getCurrentPrice()).isEqualByComparingTo("71000");
	}

	@Test
	void adminMarketPriceSyncTargetsTradedSecuritiesOnly() {
		LocalDate priceDate = LocalDate.of(2026, 5, 15);
		SecurityItem traded = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		SecurityItem holdingOnly = securityItemRepository.save(new SecurityItem("000660", "SK하이닉스", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		holdingRepository.save(new Holding(1L, account, traded, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, "KRW"));
		holdingRepository.save(new Holding(1L, account, holdingOnly, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, "KRW"));
		tradeTransactionRepository.save(new TradeTransaction(
				1L,
				account,
				traded,
				LocalDate.of(2026, 5, 10),
				TradeType.BUY,
				BigDecimal.ONE,
				BigDecimal.TEN,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				"KRW",
				TransactionSource.MANUAL,
				null
		));
		when(marketPriceProvider.fetchKospiPrices(eq(priceDate), eq(List.of("005930"))))
				.thenReturn(List.of(price("005930", priceDate, "70000")));

		AdminSyncResponse response = adminSyncService.syncMarketPrices(new AdminSyncRequest(true, priceDate, null, null, null));

		assertThat(response.status()).isEqualTo("SUCCESS");
		assertThat(marketPriceRepository.findAll()).hasSize(1);
		assertThat(marketPriceRepository.findBySecurityItemIdOrderByPriceDateDesc(traded.getId())).hasSize(1);
		assertThat(marketPriceRepository.findBySecurityItemIdOrderByPriceDateDesc(holdingOnly.getId())).isEmpty();
		assertThat(holdingRepository.findBySecurityItemId(traded.getId()).get(0).getCurrentPrice()).isEqualByComparingTo("70000");
		assertThat(holdingRepository.findBySecurityItemId(holdingOnly.getId()).get(0).getCurrentPrice()).isEqualByComparingTo("10");
		verify(marketPriceProvider).fetchKospiPrices(eq(priceDate), eq(List.of("005930")));
	}

	@Test
	void adminMarketPriceSyncStartsFromFirstTradeDateWithoutLookbackCutoff() {
		LocalDate firstTradeDate = LocalDate.now().minusDays(90);
		SecurityItem traded = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, traded, firstTradeDate);
		when(marketPriceProvider.fetchKospiPrices(any(LocalDate.class), eq(List.of("005930"))))
				.thenAnswer(invocation -> {
					LocalDate priceDate = invocation.getArgument(0);
					return List.of(price("005930", priceDate, "70000"));
				});

		AdminSyncResponse response = adminSyncService.syncMarketPrices(new AdminSyncRequest(false, null, null, null, null));

		assertThat(response.status()).isEqualTo("SUCCESS");
		assertThat(response.basDd()).startsWith(firstTradeDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
		assertThat(marketPriceRepository.existsBySecurityItemIdAndPriceDateAndSource(traded.getId(), firstTradeDate, MarketDataSource.KRX)).isTrue();
	}

	@Test
	void adminMarketPriceSyncRetriesDateWhenOnlySomeTargetSecuritiesExistEvenIfStatusSucceeded() {
		LocalDate priceDate = LocalDate.of(2026, 5, 15);
		SecurityItem existing = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		SecurityItem missing = securityItemRepository.save(new SecurityItem("000660", "SK하이닉스", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, existing, priceDate.minusDays(1));
		addTrade(account, missing, priceDate.minusDays(1));
		marketPriceSyncService.upsertImportedPrices(List.of(price("005930", priceDate, "70000")));
		dataSyncStatusService.markSuccess(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, "TRADED_SECURITIES_20260515", priceDate, "partial old success");
		when(marketPriceProvider.fetchKospiPrices(eq(priceDate), eq(List.of("000660"))))
				.thenReturn(List.of(price("000660", priceDate, "120000")));

		AdminSyncResponse response = adminSyncService.syncMarketPrices(new AdminSyncRequest(false, priceDate, null, null, null));

		assertThat(response.status()).isEqualTo("SUCCESS");
		assertThat(marketPriceRepository.existsBySecurityItemIdAndPriceDateAndSource(existing.getId(), priceDate, MarketDataSource.KRX)).isTrue();
		assertThat(marketPriceRepository.existsBySecurityItemIdAndPriceDateAndSource(missing.getId(), priceDate, MarketDataSource.KRX)).isTrue();
		verify(marketPriceProvider).fetchKospiPrices(eq(priceDate), eq(List.of("000660")));
	}

	@Test
	void adminStockDividendSyncDoesNotSkipOnlyBecauseTodayAlreadySucceeded() {
		LocalDate recordDate = LocalDate.of(2020, 12, 31);
		SecurityItem security = securityItemRepository.save(new SecurityItem("002020", "코오롱", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, security, LocalDate.of(2020, 1, 2));
		dataSyncStatusService.markSuccess(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, "TRADED_STOCK_SECURITIES", LocalDate.now(), "old success");
		when(stockDividendProvider.fetch("코오롱"))
				.thenReturn(new StockDividendFetchResult("코오롱", 200, "00", "NORMAL_SERVICE", 1, List.of(
						new ImportedDividendEvent(
								"코오롱",
								"KR7002020000",
								"코오롱",
								"코오롱",
								"보통주",
								"현금배당",
								recordDate,
								LocalDate.of(2021, 4, 16),
								new BigDecimal("1200")
						)
				)));

		AdminSyncResponse response = adminSyncService.syncStockDividends(new AdminSyncRequest(false, null, null, null, null));

		assertThat(response.status()).isEqualTo("SUCCESS");
		assertThat(dividendEventRepository.findBySecurityItemId(security.getId())).hasSize(1);
		assertThat(dividendPaymentRepository.findByUserId(1L)).hasSize(1);
	}

	private void addTrade(Account account, SecurityItem securityItem, LocalDate tradeDate) {
		tradeTransactionRepository.save(new TradeTransaction(
				1L,
				account,
				securityItem,
				tradeDate,
				TradeType.BUY,
				BigDecimal.ONE,
				BigDecimal.TEN,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				"KRW",
				TransactionSource.MANUAL,
				null
		));
	}

	private ImportedMarketPrice price(String ticker, LocalDate priceDate, String currentPrice) {
		BigDecimal price = new BigDecimal(currentPrice);
		return new ImportedMarketPrice(
				ticker,
				priceDate,
				price,
				price,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				null,
				null,
				null,
				1000L,
				null,
				null,
				null,
				null,
				MarketDataSource.KRX
		);
	}
}
