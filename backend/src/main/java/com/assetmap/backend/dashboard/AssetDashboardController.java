package com.assetmap.backend.dashboard;

import com.assetmap.backend.common.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetDashboardController {

	private final AssetDashboardService assetDashboardService;

	public AssetDashboardController(AssetDashboardService assetDashboardService) {
		this.assetDashboardService = assetDashboardService;
	}

	@GetMapping("/summary")
	public ApiResponse<AssetSummaryResponse> summary() {
		return ApiResponse.success(assetDashboardService.summary());
	}

	@GetMapping("/by-account")
	public ApiResponse<List<AccountAssetRatioResponse>> byAccount() {
		return ApiResponse.success(assetDashboardService.byAccount());
	}

	@GetMapping("/by-country")
	public ApiResponse<List<AssetRatioResponse>> byCountry() {
		return ApiResponse.success(assetDashboardService.byCountry());
	}

	@GetMapping("/by-type")
	public ApiResponse<List<AssetRatioResponse>> byType() {
		return ApiResponse.success(assetDashboardService.byType());
	}

	@GetMapping("/by-sector")
	public ApiResponse<List<AssetRatioResponse>> bySector() {
		return ApiResponse.success(assetDashboardService.bySector());
	}

	@GetMapping("/by-strategy")
	public ApiResponse<List<AssetRatioResponse>> byStrategy() {
		return ApiResponse.success(assetDashboardService.byStrategy());
	}
}
