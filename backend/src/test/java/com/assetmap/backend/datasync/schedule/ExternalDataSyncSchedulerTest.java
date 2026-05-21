package com.assetmap.backend.datasync.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.assetmap.backend.datasync.admin.AdminSyncService;
import com.assetmap.backend.datasync.admin.dto.AdminSyncRequest;
import com.assetmap.backend.datasync.admin.dto.AdminSyncResponse;
import com.assetmap.backend.datasync.common.DataSyncSource;
import com.assetmap.backend.datasync.common.DataSyncType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalDataSyncSchedulerTest {

	@Mock
	private AdminSyncService adminSyncService;

	@Test
	void scheduledMarketPricesUsesDailyPortfolioWhenHoldingSnapshotsAreEnabled() {
		when(adminSyncService.syncDailyPortfolio(any(AdminSyncRequest.class))).thenReturn(response("SUCCESS"));
		ExternalDataSyncScheduler scheduler = new ExternalDataSyncScheduler(adminSyncService, false, true, true, false);

		scheduler.syncMarketPrices();

		verify(adminSyncService).syncDailyPortfolio(any(AdminSyncRequest.class));
		verify(adminSyncService, never()).syncMarketPrices(any(AdminSyncRequest.class));
		verify(adminSyncService, never()).syncHoldingSnapshots(any(AdminSyncRequest.class));
	}

	@Test
	void scheduledMarketPricesKeepsMarketPriceOnlyBehaviorWhenHoldingSnapshotsAreDisabled() {
		when(adminSyncService.syncMarketPrices(any(AdminSyncRequest.class))).thenReturn(response("SUCCESS"));
		ExternalDataSyncScheduler scheduler = new ExternalDataSyncScheduler(adminSyncService, false, true, false, false);

		scheduler.syncMarketPrices();

		verify(adminSyncService).syncMarketPrices(any(AdminSyncRequest.class));
		verify(adminSyncService, never()).syncDailyPortfolio(any(AdminSyncRequest.class));
		verify(adminSyncService, never()).syncHoldingSnapshots(any(AdminSyncRequest.class));
	}

	private AdminSyncResponse response(String status) {
		return new AdminSyncResponse(
				status,
				DataSyncType.MARKET_PRICE,
				DataSyncSource.KRX,
				"TRADED_SECURITIES",
				"",
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				"ok",
				null
		);
	}
}
