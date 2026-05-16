package com.assetmap.backend.securityitem;

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
@RequestMapping("/api/securities")
public class SecurityItemController {

	private final SecurityItemService securityItemService;

	public SecurityItemController(SecurityItemService securityItemService) {
		this.securityItemService = securityItemService;
	}

	@PostMapping
	public ApiResponse<SecurityItemResponse> create(@Valid @RequestBody SecurityItemCreateRequest request) {
		return ApiResponse.success(securityItemService.create(request));
	}

	@GetMapping
	public ApiResponse<List<SecurityItemResponse>> findAll() {
		return ApiResponse.success(securityItemService.findAll());
	}

	@GetMapping("/{securityId}")
	public ApiResponse<SecurityItemResponse> findById(@PathVariable Long securityId) {
		return ApiResponse.success(securityItemService.findById(securityId));
	}

	@PatchMapping("/{securityId}")
	public ApiResponse<SecurityItemResponse> update(
			@PathVariable Long securityId,
			@Valid @RequestBody SecurityItemUpdateRequest request
	) {
		return ApiResponse.success(securityItemService.update(securityId, request));
	}

	@DeleteMapping("/{securityId}")
	public ApiResponse<Void> delete(@PathVariable Long securityId) {
		securityItemService.delete(securityId);
		return ApiResponse.successWithoutData();
	}
}
