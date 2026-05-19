package com.assetmap.backend.datasync;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetmap.backend.account.Account;
import com.assetmap.backend.account.AccountRepository;
import com.assetmap.backend.account.AccountType;
import com.assetmap.backend.datasync.provider.ImportedMarketPrice;
import com.assetmap.backend.datasync.provider.ImportedSecurityMaster;
import com.assetmap.backend.holding.Holding;
import com.assetmap.backend.holding.HoldingRepository;
import com.assetmap.backend.marketprice.MarketDataSource;
import com.assetmap.backend.marketprice.MarketPriceRepository;
import com.assetmap.backend.securityitem.SecurityItem;
import com.assetmap.backend.securityitem.SecurityItemRepository;
import com.assetmap.backend.securityitem.SecurityType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DataSyncInfrastructureTest {

	@Autowired
	private DataSyncStatusService dataSyncStatusService;
	@Autowired
	private DataSyncStatusRepository dataSyncStatusRepository;
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

	@BeforeEach
	void setUp() {
		marketPriceRepository.deleteAll();
		holdingRepository.deleteAll();
		accountRepository.deleteAll();
		securityItemRepository.deleteAll();
		dataSyncStatusRepository.deleteAll();
	}

	@Test
	void dataSyncStatusMarksAndChecksToday() {
		assertThat(dataSyncStatusService.shouldSyncToday(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", false)).isTrue();

		dataSyncStatusService.markRunning(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", "running");
		dataSyncStatusService.markSuccess(DataSyncType.SECURITY_MASTER, DataSyncSource.KRX, "ALL", LocalDate.now(), "done");

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
