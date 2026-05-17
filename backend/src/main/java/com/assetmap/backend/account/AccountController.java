package com.assetmap.backend.account;

import com.assetmap.backend.auth.SecurityUtil;
import com.assetmap.backend.common.response.ApiResponse;
import com.assetmap.backend.dividend.DividendPaymentResponse;
import com.assetmap.backend.dividend.DividendPaymentService;
import com.assetmap.backend.holding.HoldingResponse;
import com.assetmap.backend.holding.HoldingService;
import com.assetmap.backend.transaction.TradeTransactionResponse;
import com.assetmap.backend.transaction.TradeTransactionService;
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
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;
	private final HoldingService holdingService;
	private final TradeTransactionService transactionService;
	private final DividendPaymentService paymentService;

	public AccountController(
			AccountService accountService,
			HoldingService holdingService,
			TradeTransactionService transactionService,
			DividendPaymentService paymentService
	) {
		this.accountService = accountService;
		this.holdingService = holdingService;
		this.transactionService = transactionService;
		this.paymentService = paymentService;
	}

	@PostMapping
	public ApiResponse<AccountResponse> create(@Valid @RequestBody AccountCreateRequest request) {
		return ApiResponse.success(accountService.create(SecurityUtil.getCurrentUserId(), request));
	}

	@GetMapping
	public ApiResponse<List<AccountResponse>> findAll() {
		return ApiResponse.success(accountService.findAll(SecurityUtil.getCurrentUserId()));
	}

	@GetMapping("/{accountId}")
	public ApiResponse<AccountResponse> findById(@PathVariable Long accountId) {
		return ApiResponse.success(accountService.findById(SecurityUtil.getCurrentUserId(), accountId));
	}

	@GetMapping("/{accountId}/holdings")
	public ApiResponse<List<HoldingResponse>> holdings(@PathVariable Long accountId) {
		return ApiResponse.success(holdingService.findByAccount(SecurityUtil.getCurrentUserId(), accountId));
	}

	@GetMapping("/{accountId}/trades")
	public ApiResponse<List<TradeTransactionResponse>> trades(@PathVariable Long accountId) {
		return ApiResponse.success(transactionService.findByAccount(SecurityUtil.getCurrentUserId(), accountId));
	}

	@GetMapping("/{accountId}/dividend-payments")
	public ApiResponse<List<DividendPaymentResponse>> dividendPayments(@PathVariable Long accountId) {
		return ApiResponse.success(paymentService.findByAccount(SecurityUtil.getCurrentUserId(), accountId));
	}

	@PatchMapping("/{accountId}")
	public ApiResponse<AccountResponse> update(
			@PathVariable Long accountId,
			@Valid @RequestBody AccountUpdateRequest request
	) {
		return ApiResponse.success(accountService.update(SecurityUtil.getCurrentUserId(), accountId, request));
	}

	@DeleteMapping("/{accountId}")
	public ApiResponse<Void> delete(@PathVariable Long accountId) {
		accountService.delete(SecurityUtil.getCurrentUserId(), accountId);
		return ApiResponse.successWithoutData();
	}
}
