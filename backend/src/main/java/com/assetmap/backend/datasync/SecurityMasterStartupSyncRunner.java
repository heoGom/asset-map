package com.assetmap.backend.datasync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@ConditionalOnProperty(name = "app.sync.security-master-on-startup", havingValue = "true")
public class SecurityMasterStartupSyncRunner {

	private static final Logger log = LoggerFactory.getLogger(SecurityMasterStartupSyncRunner.class);

	private final AdminSyncService adminSyncService;

	public SecurityMasterStartupSyncRunner(AdminSyncService adminSyncService) {
		this.adminSyncService = adminSyncService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void syncSecurityMasterOnStartup() {
		try {
			AdminSyncResponse response = adminSyncService.syncSecurityMaster(new AdminSyncRequest(false, null, null));
			log.info("Startup security master sync checked. status={} message={}", response.status(), response.message());
		} catch (RuntimeException exception) {
			log.warn("Startup security master sync failed but application remains running. reason={}", exception.toString());
		}
	}
}
