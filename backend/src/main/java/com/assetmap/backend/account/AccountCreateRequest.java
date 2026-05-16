package com.assetmap.backend.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountCreateRequest(
		@NotNull Long userId,
		@NotBlank String name,
		String brokerName,
		@NotNull AccountType accountType,
		@NotBlank String currency,
		String memo
) {
}
