package com.assetmap.backend.datasync.admin;
import com.assetmap.backend.datasync.admin.AdminSyncStatusDetailService;
import com.assetmap.backend.datasync.admin.AdminSyncService;
import com.assetmap.backend.datasync.admin.dto.DataSyncStatusResponse;
import com.assetmap.backend.datasync.admin.dto.AdminSyncStatusDetailResponse;
import com.assetmap.backend.datasync.admin.dto.AdminSyncResponse;
import com.assetmap.backend.datasync.admin.dto.AdminSyncRequest;

import com.assetmap.backend.common.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sync")
public class AdminSyncController {

	private final AdminSyncService adminSyncService;
	private final AdminSyncStatusDetailService adminSyncStatusDetailService;

	public AdminSyncController(AdminSyncService adminSyncService, AdminSyncStatusDetailService adminSyncStatusDetailService) {
		this.adminSyncService = adminSyncService;
		this.adminSyncStatusDetailService = adminSyncStatusDetailService;
	}

	@GetMapping("/status")
	public ApiResponse<List<DataSyncStatusResponse>> statuses() {
		return ApiResponse.success(adminSyncService.getStatuses());
	}

	@GetMapping("/status/detail")
	public ApiResponse<AdminSyncStatusDetailResponse> statusDetail() {
		return ApiResponse.success(adminSyncStatusDetailService.getDetail());
	}

	@PostMapping("/security-master")
	public ApiResponse<AdminSyncResponse> syncSecurityMaster(@RequestBody(required = false) AdminSyncRequest request) {
		return ApiResponse.success(adminSyncService.syncSecurityMaster(request));
	}

	@PostMapping("/market-prices")
	public ApiResponse<AdminSyncResponse> syncMarketPrices(@RequestBody(required = false) AdminSyncRequest request) {
		return ApiResponse.success(adminSyncService.syncMarketPrices(request));
	}

	@PostMapping("/holding-snapshots")
	public ApiResponse<AdminSyncResponse> syncHoldingSnapshots(@RequestBody(required = false) AdminSyncRequest request) {
		return ApiResponse.success(adminSyncService.syncHoldingSnapshots(request));
	}

	@PostMapping("/daily-portfolio")
	public ApiResponse<AdminSyncResponse> syncDailyPortfolio(@RequestBody(required = false) AdminSyncRequest request) {
		return ApiResponse.success(adminSyncService.syncDailyPortfolio(request));
	}

	@PostMapping("/stock-dividends")
	public ApiResponse<AdminSyncResponse> syncStockDividends(@RequestBody(required = false) AdminSyncRequest request) {
		return ApiResponse.success(adminSyncService.syncStockDividends(request));
	}
}
