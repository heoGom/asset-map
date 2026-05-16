package com.assetmap.backend.account;

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
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	public ApiResponse<AccountResponse> create(@Valid @RequestBody AccountCreateRequest request) {
		return ApiResponse.success(accountService.create(request));
	}

	@GetMapping
	public ApiResponse<List<AccountResponse>> findAll() {
		return ApiResponse.success(accountService.findAll());
	}

	@GetMapping("/{accountId}")
	public ApiResponse<AccountResponse> findById(@PathVariable Long accountId) {
		return ApiResponse.success(accountService.findById(accountId));
	}

	@PatchMapping("/{accountId}")
	public ApiResponse<AccountResponse> update(
			@PathVariable Long accountId,
			@Valid @RequestBody AccountUpdateRequest request
	) {
		return ApiResponse.success(accountService.update(accountId, request));
	}

	@DeleteMapping("/{accountId}")
	public ApiResponse<Void> delete(@PathVariable Long accountId) {
		accountService.delete(accountId);
		return ApiResponse.successWithoutData();
	}
}
