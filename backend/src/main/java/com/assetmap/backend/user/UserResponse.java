package com.assetmap.backend.user;

import java.time.LocalDateTime;

public record UserResponse(
		Long id,
		String email,
		String nickname,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static UserResponse from(AppUser user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getNickname(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
