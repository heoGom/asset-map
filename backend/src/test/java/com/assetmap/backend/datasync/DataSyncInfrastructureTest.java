package com.assetmap.backend.datasync;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.account.AccountRepository;
import com.assetmap.backend.account.AccountType;
import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.datasync.provider.MarketPriceProvider;
import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.dividend.DataSourceType;
import com.assetmap.backend.dividend.DividendEvent;
import com.assetmap.backend.dividend.DividendEventRepository;
import com.assetmap.backend.dividend.DividendEventType;
import com.assetmap.backend.dividend.DividendPaymentRepository;
import com.assetmap.backend.dividend.importer.dto.ImportedDividendEvent;
import com.assetmap.backend.dividend.importer.dto.StockDividendFetchResult;
import com.assetmap.backend.dividend.importer.provider.StockDividendProvider;
import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPrice;
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
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class DataSyncInfrastructureTest {

	@Autowired
	private DataSyncStatusService dataSyncStatusService;
	@Autowired
	private DataSyncStatusRepository dataSyncStatusRepository;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private AdminSyncService adminSyncService;
	@Autowired
	private SyncPlanService syncPlanService;
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
		dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", "failed once");
		dataSyncStatusService.markSuccess(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", LocalDate.now(), "done again");
		dataSyncStatusService.markSkipped(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", "already synced");

		assertThat(dataSyncStatusService.shouldSyncToday(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", false)).isFalse();
		assertThat(dataSyncStatusService.shouldSyncToday(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", true)).isTrue();
		assertThat(dataSyncStatusService.getStatus(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL").status()).isEqualTo(DataSyncStatusValue.SUCCESS);
		assertThat(dataSyncStatusService.getStatus(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL").lastFailureAt()).isNotNull();
	}

	@Test
	void adminSyncStatusDetailReportsDbGapsAndStatusCheckpointsWithoutExternalApiCalls() throws Exception {
		LocalDate today = LocalDate.now();
		int currentYear = today.getYear();
		SecurityItem pricedStock = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		SecurityItem missingEtf = securityItemRepository.save(new SecurityItem("133690", "TIGER 미국나스닥100", "ETF", "KOREA", "KRW", SecurityType.ETF));
		SecurityItem dividendStock = securityItemRepository.save(new SecurityItem("002020", "코오롱", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		SecurityItem missingDividendStock = securityItemRepository.save(new SecurityItem("000660", "SK하이닉스", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, pricedStock, today.minusDays(1));
		addTrade(account, missingEtf, today.minusDays(1));
		addTrade(account, dividendStock, today.minusDays(1));
		addTrade(account, missingDividendStock, today.minusDays(1));
		marketPriceRepository.save(new MarketPrice(
				pricedStock,
				today.minusDays(1),
				new BigDecimal("70000"),
				new BigDecimal("70000"),
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				100L,
				MarketDataSource.KRX,
				LocalDateTime.now()
		));
		dividendEventRepository.save(new DividendEvent(
				dividendStock,
				currentYear,
				null,
				null,
				LocalDate.of(currentYear, 12, 31),
				LocalDate.of(currentYear, 4, 16),
				DividendEventType.CASH_DIVIDEND,
				new BigDecimal("1200"),
				"KRW",
				DataSourceType.PUBLIC_DATA_STOCK_DIVIDEND
		));
		dataSyncStatusService.markSuccess(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", today, "security master ok");
		dataSyncStatusService.markFailed(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", "security master failed once");
		dataSyncStatusService.markSuccess(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", today, "security master ok again");
		dataSyncStatusService.markNoData(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, "TRADED_SECURITIES_" + today.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE), today, "fresh market no data");
		dataSyncStatusService.markNoData(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, "TRADED_SECURITIES_" + today.minusDays(2).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE), today.minusDays(2), "expired market no data");
		expireStatus(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, "TRADED_SECURITIES_" + today.minusDays(2).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
		dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, "TRADED_SECURITIES_" + today.minusDays(3).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE), "market failed");
		dataSyncStatusService.markNoData(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, ExternalDataSyncCheckpointService.stockDividendTargetKey(pricedStock, currentYear), LocalDate.of(currentYear, 12, 31), "fresh dividend no data");
		dataSyncStatusService.markNoData(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, ExternalDataSyncCheckpointService.stockDividendTargetKey(dividendStock, currentYear - 1), LocalDate.of(currentYear - 1, 12, 31), "expired dividend no data");
		expireStatus(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, ExternalDataSyncCheckpointService.stockDividendTargetKey(dividendStock, currentYear - 1));
		dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, ExternalDataSyncCheckpointService.stockDividendTargetKey(missingDividendStock, currentYear - 1), "dividend failed");

		mockMvc.perform(get("/api/admin/sync/status/detail"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.securityMaster.status").value("SUCCESS"))
				.andExpect(jsonPath("$.data.securityMaster.lastSuccessAt").exists())
				.andExpect(jsonPath("$.data.securityMaster.lastFailureAt").exists())
				.andExpect(jsonPath("$.data.marketPrices.totalTargetSecurityCount").value(4))
				.andExpect(jsonPath("$.data.marketPrices.pricedSecurityCount").value(1))
				.andExpect(jsonPath("$.data.marketPrices.missingSecurityCount").value(3))
				.andExpect(jsonPath("$.data.marketPrices.pendingDateCount").value(1))
				.andExpect(jsonPath("$.data.marketPrices.freshNoDataDateCount").value(1))
				.andExpect(jsonPath("$.data.marketPrices.expiredNoDataDateCount").value(1))
				.andExpect(jsonPath("$.data.marketPrices.recentFailedDates[0].message").value("market failed"))
				.andExpect(jsonPath("$.data.stockDividends.totalTargetSecurityCount").value(3))
				.andExpect(jsonPath("$.data.stockDividends.eventSecurityCount").value(1))
				.andExpect(jsonPath("$.data.stockDividends.missingOrRecheckSecurityYearCount").value(2))
				.andExpect(jsonPath("$.data.stockDividends.freshNoDataSecurityYearCount").value(1))
				.andExpect(jsonPath("$.data.stockDividends.expiredNoDataSecurityYearCount").value(1))
				.andExpect(jsonPath("$.data.stockDividends.recentFailedSecurityYears[0].message").value("dividend failed"));

		verify(marketPriceProvider, never()).fetchKospiPrices(any(LocalDate.class), anyList());
		verify(marketPriceProvider, never()).fetchKosdaqPrices(any(LocalDate.class), anyList());
		verify(marketPriceProvider, never()).fetchEtfPrices(any(LocalDate.class), anyList());
		verify(stockDividendProvider, never()).fetch(any(String.class));
	}

	@Test
	void syncPlanServiceIsSharedBySyncExecutionAndStatusDetailPendingCounts() throws Exception {
		LocalDate today = LocalDate.now();
		int currentYear = today.getYear();
		SecurityItem marketTarget = securityItemRepository.save(new SecurityItem("133690", "TIGER 미국나스닥100", "ETF", "KOREA", "KRW", SecurityType.ETF));
		SecurityItem freshDividendTarget = securityItemRepository.save(new SecurityItem("002020", "코오롱", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		SecurityItem expiredDividendTarget = securityItemRepository.save(new SecurityItem("000660", "SK하이닉스", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		SecurityItem failedDividendTarget = securityItemRepository.save(new SecurityItem("005935", "삼성전자우", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, marketTarget, today);
		addTrade(account, freshDividendTarget, today);
		addTrade(account, expiredDividendTarget, today);
		addTrade(account, failedDividendTarget, today);
		String marketTargetKey = "TRADED_SECURITIES_" + today.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
		dataSyncStatusService.markNoData(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, marketTargetKey, today, "fresh market no data");
		dataSyncStatusService.markNoData(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, ExternalDataSyncCheckpointService.stockDividendTargetKey(freshDividendTarget, currentYear), LocalDate.of(currentYear, 12, 31), "fresh dividend no data");
		dataSyncStatusService.markNoData(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, ExternalDataSyncCheckpointService.stockDividendTargetKey(expiredDividendTarget, currentYear), LocalDate.of(currentYear, 12, 31), "expired dividend no data");
		expireStatus(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, ExternalDataSyncCheckpointService.stockDividendTargetKey(expiredDividendTarget, currentYear));
		dataSyncStatusService.markFailed(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, ExternalDataSyncCheckpointService.stockDividendTargetKey(failedDividendTarget, currentYear), "failed dividend");

		MarketPriceSyncPlan freshMarketPlan = syncPlanService.planMarketPrices(new AdminSyncRequest(false, today, null, null, null), false, null);
		assertThat(freshMarketPlan.dateTargets()).isEmpty();
		expireStatus(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, marketTargetKey);
		MarketPriceSyncPlan expiredMarketPlan = syncPlanService.planMarketPrices(new AdminSyncRequest(false, today, null, null, null), false, null);
		assertThat(expiredMarketPlan.dateTargets()).hasSize(1);
		dataSyncStatusService.markFailed(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, marketTargetKey, "failed market");
		MarketPriceSyncPlan failedMarketPlan = syncPlanService.planMarketPrices(new AdminSyncRequest(false, today, null, null, null), false, null);
		assertThat(failedMarketPlan.dateTargets()).hasSize(1);
		StockDividendSyncPlan dividendPlan = syncPlanService.planStockDividends(new AdminSyncRequest(false, null, null, currentYear, currentYear), false);
		assertThat(dividendPlan.yearTargets())
				.extracting(target -> target.securityItem().getId())
				.containsExactlyInAnyOrder(expiredDividendTarget.getId(), failedDividendTarget.getId());

		mockMvc.perform(get("/api/admin/sync/status/detail"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.marketPrices.pendingDateCount").value(failedMarketPlan.dateTargets().size()))
				.andExpect(jsonPath("$.data.stockDividends.missingOrRecheckSecurityYearCount").value(dividendPlan.yearTargets().size()));

		verify(marketPriceProvider, never()).fetchKospiPrices(any(LocalDate.class), anyList());
		verify(marketPriceProvider, never()).fetchKosdaqPrices(any(LocalDate.class), anyList());
		verify(marketPriceProvider, never()).fetchEtfPrices(any(LocalDate.class), anyList());
		verify(stockDividendProvider, never()).fetch(any(String.class));
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
	void adminMarketPriceSyncPrioritizesLatestDatesWhenBackfillHasLargeGap() {
		LocalDate firstTradeDate = LocalDate.now().minusDays(90);
		LocalDate firstSelectedDate = LocalDate.now().minusDays(59);
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
		assertThat(response.basDd()).startsWith(firstSelectedDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
		assertThat(response.basDd()).endsWith(LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
		assertThat(marketPriceRepository.existsBySecurityItemIdAndPriceDateAndSource(traded.getId(), LocalDate.now(), MarketDataSource.KRX)).isTrue();
		assertThat(marketPriceRepository.existsBySecurityItemIdAndPriceDateAndSource(traded.getId(), firstTradeDate, MarketDataSource.KRX)).isFalse();
	}

	@Test
	void adminMarketPriceSyncRunsFromDateCheckpointsEvenWhenRepresentativeCheckpointIsRunning() {
		LocalDate priceDate = LocalDate.now();
		SecurityItem traded = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, traded, priceDate);
		dataSyncStatusService.markRunning(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, AdminSyncService.TRADED_SECURITIES, "stale representative running");
		when(marketPriceProvider.fetchKospiPrices(eq(priceDate), eq(List.of("005930"))))
				.thenReturn(List.of(price("005930", priceDate, "70000")));

		MarketPriceSyncPlan plan = syncPlanService.planMarketPrices(new AdminSyncRequest(false, null, null, null, null), false, 60);
		AdminSyncResponse response = adminSyncService.syncMarketPrices(new AdminSyncRequest(false, null, null, null, null));

		assertThat(plan.dateTargets()).extracting(MarketPriceDateTarget::priceDate).containsExactly(priceDate);
		assertThat(response.status()).isEqualTo("SUCCESS");
		assertThat(dataSyncStatusService.getStatus(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, AdminSyncService.TRADED_SECURITIES).status()).isEqualTo(DataSyncStatusValue.SUCCESS);
		assertThat(dataSyncStatusService.getStatus(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, "TRADED_SECURITIES_" + priceDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)).status()).isEqualTo(DataSyncStatusValue.SUCCESS);
		assertThat(marketPriceRepository.existsBySecurityItemIdAndPriceDateAndSource(traded.getId(), priceDate, MarketDataSource.KRX)).isTrue();
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
	void marketPriceNoDataSkipsUntilRecheckTtlExpires() {
		LocalDate priceDate = LocalDate.of(2026, 5, 15);
		SecurityItem traded = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, traded, priceDate.minusDays(1));
		String targetKey = "TRADED_SECURITIES_20260515";
		dataSyncStatusService.markNoData(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey, priceDate, "fresh no data");

		AdminSyncResponse skipped = adminSyncService.syncMarketPrices(new AdminSyncRequest(false, priceDate, null, null, null));

		assertThat(skipped.status()).isEqualTo("SKIPPED");
		verify(marketPriceProvider, never()).fetchKospiPrices(eq(priceDate), eq(List.of("005930")));

		DataSyncStatus status = dataSyncStatusRepository.findBySyncTypeAndSourceAndTargetKey(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, targetKey).orElseThrow();
		ReflectionTestUtils.setField(status, "lastSuccessAt", LocalDateTime.now().minusDays(8));
		dataSyncStatusRepository.saveAndFlush(status);
		when(marketPriceProvider.fetchKospiPrices(eq(priceDate), eq(List.of("005930"))))
				.thenReturn(List.of(price("005930", priceDate, "70000")));

		AdminSyncResponse retried = adminSyncService.syncMarketPrices(new AdminSyncRequest(false, priceDate, null, null, null));

		assertThat(retried.status()).isEqualTo("SUCCESS");
		assertThat(marketPriceRepository.existsBySecurityItemIdAndPriceDateAndSource(traded.getId(), priceDate, MarketDataSource.KRX)).isTrue();
		verify(marketPriceProvider).fetchKospiPrices(eq(priceDate), eq(List.of("005930")));
	}

	@Test
	void marketPriceApiFailureIsRecordedAsFailedNotNoData() {
		LocalDate priceDate = LocalDate.of(2026, 5, 15);
		SecurityItem traded = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, traded, priceDate.minusDays(1));
		when(marketPriceProvider.fetchKospiPrices(eq(priceDate), eq(List.of("005930"))))
				.thenThrow(new BusinessException(ErrorCode.API_RESPONSE_ERROR, "KRX failure"));

		AdminSyncResponse response = adminSyncService.syncMarketPrices(new AdminSyncRequest(false, priceDate, null, null, null));

		assertThat(response.status()).isEqualTo("FAILED");
		DataSyncStatus status = dataSyncStatusRepository.findBySyncTypeAndSourceAndTargetKey(DataSyncType.MARKET_PRICE, DataSyncSource.KRX, "TRADED_SECURITIES_20260515").orElseThrow();
		assertThat(status.getStatus()).isEqualTo(DataSyncStatusValue.FAILED);
	}

	@Test
	void marketPricePartialDateFailureKeepsPreviouslyCommittedDate() {
		LocalDate firstDate = LocalDate.now().minusDays(1);
		LocalDate secondDate = LocalDate.now();
		SecurityItem traded = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, traded, firstDate);
		when(marketPriceProvider.fetchKospiPrices(eq(firstDate), eq(List.of("005930"))))
				.thenReturn(List.of(price("005930", firstDate, "70000")));
		when(marketPriceProvider.fetchKospiPrices(eq(secondDate), eq(List.of("005930"))))
				.thenThrow(new BusinessException(ErrorCode.API_RESPONSE_ERROR, "KRX failure"));

		AdminSyncResponse response = adminSyncService.syncMarketPrices(new AdminSyncRequest(false, null, null, null, null));

		assertThat(response.status()).isEqualTo("SUCCESS");
		assertThat(response.failedCount()).isEqualTo(1);
		assertThat(marketPriceRepository.existsBySecurityItemIdAndPriceDateAndSource(traded.getId(), firstDate, MarketDataSource.KRX)).isTrue();
		DataSyncStatus failedStatus = dataSyncStatusRepository.findBySyncTypeAndSourceAndTargetKey(
				DataSyncType.MARKET_PRICE,
				DataSyncSource.KRX,
				"TRADED_SECURITIES_" + secondDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
		).orElseThrow();
		assertThat(failedStatus.getStatus()).isEqualTo(DataSyncStatusValue.FAILED);
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

	@Test
	void stockDividendNoDataSkipsUntilRecheckTtlExpires() {
		int year = 2020;
		SecurityItem security = securityItemRepository.save(new SecurityItem("002020", "코오롱", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, security, LocalDate.of(year, 1, 2));
		String targetKey = ExternalDataSyncCheckpointService.stockDividendTargetKey(security, year);
		dataSyncStatusService.markNoData(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, targetKey, LocalDate.of(year, 12, 31), "fresh no data");

		AdminSyncResponse skipped = adminSyncService.syncStockDividends(new AdminSyncRequest(false, null, null, year, year));

		assertThat(skipped.status()).isEqualTo("SKIPPED");
		verify(stockDividendProvider, never()).fetch("코오롱");

		DataSyncStatus status = dataSyncStatusRepository.findBySyncTypeAndSourceAndTargetKey(DataSyncType.STOCK_DIVIDEND, DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND, targetKey).orElseThrow();
		ReflectionTestUtils.setField(status, "lastSuccessAt", LocalDateTime.now().minusDays(8));
		dataSyncStatusRepository.saveAndFlush(status);
		when(stockDividendProvider.fetch("코오롱"))
				.thenReturn(new StockDividendFetchResult("코오롱", 200, "00", "NORMAL_SERVICE", 0, List.of()));

		AdminSyncResponse retried = adminSyncService.syncStockDividends(new AdminSyncRequest(false, null, null, year, year));

		assertThat(retried.status()).isEqualTo("SUCCESS");
		verify(stockDividendProvider).fetch("코오롱");
	}

	@Test
	void stockDividendApiFailureIsRecordedAsFailedNotNoData() {
		int year = 2020;
		SecurityItem security = securityItemRepository.save(new SecurityItem("002020", "코오롱", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, security, LocalDate.of(year, 1, 2));
		when(stockDividendProvider.fetch("코오롱"))
				.thenReturn(new StockDividendFetchResult("코오롱", 200, "99", "SERVICE_ERROR", 0, List.of()));

		AdminSyncResponse response = adminSyncService.syncStockDividends(new AdminSyncRequest(false, null, null, year, year));

		assertThat(response.status()).isEqualTo("FAILED");
		DataSyncStatus status = dataSyncStatusRepository.findBySyncTypeAndSourceAndTargetKey(
				DataSyncType.STOCK_DIVIDEND,
				DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND,
				ExternalDataSyncCheckpointService.stockDividendTargetKey(security, year)
		).orElseThrow();
		assertThat(status.getStatus()).isEqualTo(DataSyncStatusValue.FAILED);
	}

	@Test
	void stockDividendPartialSecurityYearFailureKeepsOtherSecurityYearSuccess() {
		int year = 2020;
		LocalDate recordDate = LocalDate.of(year, 12, 31);
		SecurityItem successSecurity = securityItemRepository.save(new SecurityItem("002020", "코오롱", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		SecurityItem failedSecurity = securityItemRepository.save(new SecurityItem("005930", "삼성전자", "KOSPI", "KOREA", "KRW", SecurityType.STOCK));
		Account account = accountRepository.save(new Account(1L, "Local Account", "Local", AccountType.GENERAL, "KRW", null));
		addTrade(account, successSecurity, LocalDate.of(year, 1, 2));
		addTrade(account, failedSecurity, LocalDate.of(year, 1, 2));
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
		when(stockDividendProvider.fetch("삼성전자"))
				.thenThrow(new BusinessException(ErrorCode.API_RESPONSE_ERROR, "public data failure"));

		AdminSyncResponse response = adminSyncService.syncStockDividends(new AdminSyncRequest(false, null, null, year, year));

		assertThat(response.status()).isEqualTo("PARTIAL_SUCCESS");
		assertThat(response.failedCount()).isEqualTo(1);
		assertThat(dividendEventRepository.findBySecurityItemId(successSecurity.getId())).hasSize(1);
		DataSyncStatus failedStatus = dataSyncStatusRepository.findBySyncTypeAndSourceAndTargetKey(
				DataSyncType.STOCK_DIVIDEND,
				DataSyncSource.PUBLIC_DATA_STOCK_DIVIDEND,
				ExternalDataSyncCheckpointService.stockDividendTargetKey(failedSecurity, year)
		).orElseThrow();
		assertThat(failedStatus.getStatus()).isEqualTo(DataSyncStatusValue.FAILED);
	}

	private void expireStatus(DataSyncType syncType, DataSyncSource source, String targetKey) {
		DataSyncStatus status = dataSyncStatusRepository.findBySyncTypeAndSourceAndTargetKey(syncType, source, targetKey).orElseThrow();
		ReflectionTestUtils.setField(status, "lastSuccessAt", LocalDateTime.now().minusDays(8));
		dataSyncStatusRepository.saveAndFlush(status);
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
