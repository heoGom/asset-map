package com.assetmap.backend.transaction;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeTransactionController {

	private final TradeTransactionService transactionService;

	public TradeTransactionController(TradeTransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@PostMapping
	public ApiResponse<TradeTransactionResponse> create(@Valid @RequestBody TradeTransactionCreateRequest request) {
		return ApiResponse.success(transactionService.create(request));
	}

	@GetMapping
	public ApiResponse<List<TradeTransactionResponse>> findAll(@RequestParam(required = false) Long userId) {
		return ApiResponse.success(transactionService.findAll(userId));
	}

	@GetMapping("/{tradeId}")
	public ApiResponse<TradeTransactionResponse> findById(@PathVariable Long tradeId) {
		return ApiResponse.success(transactionService.findById(tradeId));
	}

	@PatchMapping("/{tradeId}")
	public ApiResponse<TradeTransactionResponse> update(@PathVariable Long tradeId, @Valid @RequestBody TradeTransactionUpdateRequest request) {
		return ApiResponse.success(transactionService.update(tradeId, request));
	}

	@DeleteMapping("/{tradeId}")
	public ApiResponse<Void> delete(@PathVariable Long tradeId) {
		transactionService.delete(tradeId);
		return ApiResponse.successWithoutData();
	}
}
