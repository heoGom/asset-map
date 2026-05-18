package com.assetmap.backend.datasync;

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

	public AdminSyncController(AdminSyncService adminSyncService) {
		this.adminSyncService = adminSyncService;
	}

	@GetMapping("/status")
	public ApiResponse<List<DataSyncStatusResponse>> statuses() {
		return ApiResponse.success(adminSyncService.getStatuses());
	}

	@PostMapping("/security-master")
	public ApiResponse<AdminSyncResponse> syncSecurityMaster(@RequestBody(required = false) AdminSyncRequest request) {
		return ApiResponse.success(adminSyncService.syncSecurityMaster(request));
	}

	@PostMapping("/market-prices")
	public ApiResponse<AdminSyncResponse> syncMarketPrices(@RequestBody(required = false) AdminSyncRequest request) {
		return ApiResponse.success(adminSyncService.syncMarketPrices(request));
	}
}
