package com.assetmap.backend.datasync.schedule;
import com.assetmap.backend.datasync.admin.AdminSyncService;
import com.assetmap.backend.datasync.admin.dto.AdminSyncResponse;
import com.assetmap.backend.datasync.admin.dto.AdminSyncRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(name = {"app.sync.enabled", "app.sync.on-startup.enabled"}, havingValue = "true")
public class ExternalDataStartupSyncRunner {

	private static final Logger log = LoggerFactory.getLogger(ExternalDataStartupSyncRunner.class);

	private final AdminSyncService adminSyncService;
	private final boolean securityMasterEnabled;
	private final boolean securityMasterOnStartup;
	private final boolean marketPricesEnabled;
	private final boolean marketPricesOnStartup;
	private final boolean stockDividendsEnabled;
	private final boolean stockDividendsOnStartup;

	public ExternalDataStartupSyncRunner(
			AdminSyncService adminSyncService,
			@Value("${app.sync.security-master.enabled:false}") boolean securityMasterEnabled,
			@Value("${app.sync.security-master.on-startup:false}") boolean securityMasterOnStartup,
			@Value("${app.sync.market-prices.enabled:false}") boolean marketPricesEnabled,
			@Value("${app.sync.market-prices.on-startup:false}") boolean marketPricesOnStartup,
			@Value("${app.sync.stock-dividends.enabled:false}") boolean stockDividendsEnabled,
			@Value("${app.sync.stock-dividends.on-startup:false}") boolean stockDividendsOnStartup
	) {
		this.adminSyncService = adminSyncService;
		this.securityMasterEnabled = securityMasterEnabled;
		this.securityMasterOnStartup = securityMasterOnStartup;
		this.marketPricesEnabled = marketPricesEnabled;
		this.marketPricesOnStartup = marketPricesOnStartup;
		this.stockDividendsEnabled = stockDividendsEnabled;
		this.stockDividendsOnStartup = stockDividendsOnStartup;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void syncExternalDataOnStartup() {
		if (securityMasterEnabled && securityMasterOnStartup) {
			run("security master", () -> adminSyncService.syncSecurityMaster(new AdminSyncRequest(false, null, null, null, null)));
		}
		if (marketPricesEnabled && marketPricesOnStartup) {
			run("market prices", () -> adminSyncService.syncMarketPrices(new AdminSyncRequest(false, null, null, null, null)));
		}
		if (stockDividendsEnabled && stockDividendsOnStartup) {
			run("stock dividends", () -> adminSyncService.syncStockDividends(new AdminSyncRequest(false, null, null, null, null)));
		}
	}

	private void run(String name, SyncOperation operation) {
		try {
			AdminSyncResponse response = operation.run();
			log.info("Startup external data sync checked. name={} status={} message={}", name, response.status(), response.message());
		} catch (RuntimeException exception) {
			log.warn("Startup external data sync failed but application remains running. name={} reason={}", name, exception.toString());
		}
	}

	@FunctionalInterface
	private interface SyncOperation {
		AdminSyncResponse run();
	}
}
