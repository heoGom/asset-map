package com.assetmap.backend.datasync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.sync.enabled", havingValue = "true")
public class ExternalDataSyncScheduler {

	private static final Logger log = LoggerFactory.getLogger(ExternalDataSyncScheduler.class);

	private final AdminSyncService adminSyncService;
	private final boolean securityMasterEnabled;
	private final boolean marketPricesEnabled;
	private final boolean stockDividendsEnabled;

	public ExternalDataSyncScheduler(
			AdminSyncService adminSyncService,
			@Value("${app.sync.security-master.enabled:false}") boolean securityMasterEnabled,
			@Value("${app.sync.market-prices.enabled:false}") boolean marketPricesEnabled,
			@Value("${app.sync.stock-dividends.enabled:false}") boolean stockDividendsEnabled
	) {
		this.adminSyncService = adminSyncService;
		this.securityMasterEnabled = securityMasterEnabled;
		this.marketPricesEnabled = marketPricesEnabled;
		this.stockDividendsEnabled = stockDividendsEnabled;
	}

	@Scheduled(cron = "${app.sync.security-master.cron:-}")
	public void syncSecurityMaster() {
		if (securityMasterEnabled) {
			run("scheduled security master", () -> adminSyncService.syncSecurityMaster(new AdminSyncRequest(false, null, null, null, null)));
		}
	}

	@Scheduled(cron = "${app.sync.market-prices.cron:-}")
	public void syncMarketPrices() {
		if (marketPricesEnabled) {
			run("scheduled market prices", () -> adminSyncService.syncMarketPrices(new AdminSyncRequest(false, null, null, null, null)));
		}
	}

	@Scheduled(cron = "${app.sync.stock-dividends.cron:-}")
	public void syncStockDividends() {
		if (stockDividendsEnabled) {
			run("scheduled stock dividends", () -> adminSyncService.syncStockDividends(new AdminSyncRequest(false, null, null, null, null)));
		}
	}

	private void run(String name, SyncOperation operation) {
		try {
			AdminSyncResponse response = operation.run();
			log.info("External data sync checked. name={} status={} message={}", name, response.status(), response.message());
		} catch (RuntimeException exception) {
			log.warn("External data sync failed but application remains running. name={} reason={}", name, exception.toString());
		}
	}

	@FunctionalInterface
	private interface SyncOperation {
		AdminSyncResponse run();
	}
}
