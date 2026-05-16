package com.assetmap.backend.account;

import java.time.LocalDateTime;

public record AccountResponse(
		Long id,
		Long userId,
		String name,
		String brokerName,
		AccountType accountType,
		String currency,
		String memo,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static AccountResponse from(Account account) {
		return new AccountResponse(
				account.getId(),
				account.getUserId(),
				account.getName(),
				account.getBrokerName(),
				account.getAccountType(),
				account.getCurrency(),
				account.getMemo(),
				account.getCreatedAt(),
				account.getUpdatedAt()
		);
	}
}
