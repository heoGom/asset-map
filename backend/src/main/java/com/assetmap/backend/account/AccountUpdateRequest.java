package com.assetmap.backend.account;

import jakarta.validation.constraints.Pattern;

public record AccountUpdateRequest(
		@Pattern(regexp = ".*\\S.*") String name,
		String brokerName,
		AccountType accountType,
		@Pattern(regexp = ".*\\S.*") String currency,
		String memo
) {
}
