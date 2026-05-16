package com.assetmap.backend.health;

import com.assetmap.backend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping
	public ApiResponse<HealthResponse> health() {
		return ApiResponse.success(new HealthResponse("OK"));
	}

	public record HealthResponse(String status) {
	}
}
