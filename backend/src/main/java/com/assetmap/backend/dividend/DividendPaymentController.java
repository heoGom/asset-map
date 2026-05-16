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
@RequestMapping("/api/dividends/payments")
public class DividendPaymentController {

	private final DividendPaymentService paymentService;

	public DividendPaymentController(DividendPaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ApiResponse<DividendPaymentResponse> create(@Valid @RequestBody DividendPaymentCreateRequest request) {
		return ApiResponse.success(paymentService.create(request));
	}

	@GetMapping
	public ApiResponse<List<DividendPaymentResponse>> findAll() {
		return ApiResponse.success(paymentService.findAll());
	}

	@GetMapping("/{paymentId}")
	public ApiResponse<DividendPaymentResponse> findById(@PathVariable Long paymentId) {
		return ApiResponse.success(paymentService.findById(paymentId));
	}

	@GetMapping("/user/{userId}")
	public ApiResponse<List<DividendPaymentResponse>> findByUserId(@PathVariable Long userId) {
		return ApiResponse.success(paymentService.findByUserId(userId));
	}

	@PatchMapping("/{paymentId}")
	public ApiResponse<DividendPaymentResponse> update(@PathVariable Long paymentId, @Valid @RequestBody DividendPaymentUpdateRequest request) {
		return ApiResponse.success(paymentService.update(paymentId, request));
	}

	@DeleteMapping("/{paymentId}")
	public ApiResponse<Void> delete(@PathVariable Long paymentId) {
		paymentService.delete(paymentId);
		return ApiResponse.successWithoutData();
	}
}
