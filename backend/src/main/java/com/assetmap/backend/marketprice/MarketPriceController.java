package com.assetmap.backend.marketprice;

import com.assetmap.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market-prices")
public class MarketPriceController {

	private final MarketPriceService marketPriceService;

	public MarketPriceController(MarketPriceService marketPriceService) {
		this.marketPriceService = marketPriceService;
	}

	@PostMapping
	public ApiResponse<MarketPriceResponse> create(@Valid @RequestBody MarketPriceCreateRequest request) {
		return ApiResponse.success(marketPriceService.create(request));
	}

	@GetMapping("/security/{securityItemId}")
	public ApiResponse<List<MarketPriceResponse>> findBySecurityItemId(@PathVariable Long securityItemId) {
		return ApiResponse.success(marketPriceService.findBySecurityItemId(securityItemId));
	}

	@GetMapping("/latest/security/{securityItemId}")
	public ApiResponse<MarketPriceResponse> latest(@PathVariable Long securityItemId) {
		return ApiResponse.success(marketPriceService.latest(securityItemId));
	}

	@PostMapping("/refresh")
	public ApiResponse<MarketPriceResponse> refresh(@Valid @RequestBody MarketPriceRefreshRequest request) {
		return ApiResponse.success(marketPriceService.refresh(request));
	}
}
