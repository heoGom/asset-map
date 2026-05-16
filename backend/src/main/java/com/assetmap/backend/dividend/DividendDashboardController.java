package com.assetmap.backend.dividend;

import com.assetmap.backend.auth.SecurityUtil;
import com.assetmap.backend.common.response.ApiResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dividends")
public class DividendDashboardController {

	private final DividendDashboardService dashboardService;

	public DividendDashboardController(DividendDashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/summary")
	public ApiResponse<DividendSummaryResponse> summary() {
		return ApiResponse.success(dashboardService.summary(SecurityUtil.getCurrentUserId()));
	}

	@GetMapping("/monthly")
	public ApiResponse<List<MonthlyDividendResponse>> monthly(
			@RequestParam(required = false) Integer year
	) {
		int targetYear = year == null ? LocalDate.now().getYear() : year;
		return ApiResponse.success(dashboardService.monthly(SecurityUtil.getCurrentUserId(), targetYear));
	}

	@GetMapping("/yearly")
	public ApiResponse<List<YearlyDividendResponse>> yearly() {
		return ApiResponse.success(dashboardService.yearly(SecurityUtil.getCurrentUserId()));
	}

	@GetMapping("/by-security")
	public ApiResponse<List<SecurityDividendResponse>> bySecurity() {
		return ApiResponse.success(dashboardService.bySecurity(SecurityUtil.getCurrentUserId()));
	}

	@GetMapping("/growth")
	public ApiResponse<List<DividendGrowthResponse>> growth(@RequestParam Long securityItemId) {
		return ApiResponse.success(dashboardService.growth(securityItemId));
	}
}
