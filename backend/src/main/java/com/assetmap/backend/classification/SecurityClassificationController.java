package com.assetmap.backend.classification;

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
@RequestMapping("/api/security-classifications")
public class SecurityClassificationController {

	private final SecurityClassificationService classificationService;

	public SecurityClassificationController(SecurityClassificationService classificationService) {
		this.classificationService = classificationService;
	}

	@PostMapping
	public ApiResponse<SecurityClassificationResponse> create(@Valid @RequestBody SecurityClassificationCreateRequest request) {
		return ApiResponse.success(classificationService.create(request));
	}

	@GetMapping
	public ApiResponse<List<SecurityClassificationResponse>> findAll() {
		return ApiResponse.success(classificationService.findAll());
	}

	@GetMapping("/{classificationId}")
	public ApiResponse<SecurityClassificationResponse> findById(@PathVariable Long classificationId) {
		return ApiResponse.success(classificationService.findById(classificationId));
	}

	@GetMapping("/security/{securityItemId}")
	public ApiResponse<SecurityClassificationResponse> findBySecurityItemId(@PathVariable Long securityItemId) {
		return ApiResponse.success(classificationService.findBySecurityItemId(securityItemId));
	}

	@PatchMapping("/{classificationId}")
	public ApiResponse<SecurityClassificationResponse> update(
			@PathVariable Long classificationId,
			@RequestBody SecurityClassificationUpdateRequest request
	) {
		return ApiResponse.success(classificationService.update(classificationId, request));
	}

	@DeleteMapping("/{classificationId}")
	public ApiResponse<Void> delete(@PathVariable Long classificationId) {
		classificationService.delete(classificationId);
		return ApiResponse.successWithoutData();
	}
}
