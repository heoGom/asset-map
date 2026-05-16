package com.assetmap.backend.dividend;

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
@RequestMapping("/api/dividends/events")
public class DividendEventController {

	private final DividendEventService dividendEventService;

	public DividendEventController(DividendEventService dividendEventService) {
		this.dividendEventService = dividendEventService;
	}

	@PostMapping
	public ApiResponse<DividendEventResponse> create(@Valid @RequestBody DividendEventCreateRequest request) {
		return ApiResponse.success(dividendEventService.create(request));
	}

	@GetMapping
	public ApiResponse<List<DividendEventResponse>> findAll() {
		return ApiResponse.success(dividendEventService.findAll());
	}

	@GetMapping("/{eventId}")
	public ApiResponse<DividendEventResponse> findById(@PathVariable Long eventId) {
		return ApiResponse.success(dividendEventService.findById(eventId));
	}

	@GetMapping("/security/{securityItemId}")
	public ApiResponse<List<DividendEventResponse>> findBySecurityItemId(@PathVariable Long securityItemId) {
		return ApiResponse.success(dividendEventService.findBySecurityItemId(securityItemId));
	}

	@PatchMapping("/{eventId}")
	public ApiResponse<DividendEventResponse> update(@PathVariable Long eventId, @Valid @RequestBody DividendEventUpdateRequest request) {
		return ApiResponse.success(dividendEventService.update(eventId, request));
	}

	@DeleteMapping("/{eventId}")
	public ApiResponse<Void> delete(@PathVariable Long eventId) {
		dividendEventService.delete(eventId);
		return ApiResponse.successWithoutData();
	}
}
