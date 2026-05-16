package com.assetmap.backend.holding;

import com.assetmap.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holdings")
public class HoldingController {

	private final HoldingService holdingService;

	public HoldingController(HoldingService holdingService) {
		this.holdingService = holdingService;
	}

	@PostMapping
	public ApiResponse<HoldingResponse> create(@Valid @RequestBody HoldingCreateRequest request) {
		return ApiResponse.success(holdingService.create(request));
	}

	@GetMapping
	public ApiResponse<List<HoldingResponse>> findAll() {
		return ApiResponse.success(holdingService.findAll());
	}

	@GetMapping("/{holdingId}")
	public ApiResponse<HoldingResponse> findById(@PathVariable Long holdingId) {
		return ApiResponse.success(holdingService.findById(holdingId));
	}

	@PatchMapping("/{holdingId}")
	public ApiResponse<HoldingResponse> update(
			@PathVariable Long holdingId,
			@Valid @RequestBody HoldingUpdateRequest request
	) {
		return ApiResponse.success(holdingService.update(holdingId, request));
	}

	@DeleteMapping("/{holdingId}")
	public ApiResponse<Void> delete(@PathVariable Long holdingId) {
		holdingService.delete(holdingId);
		return ApiResponse.successWithoutData();
	}
}
