package com.assetmap.backend.auth;

import com.assetmap.backend.user.UserResponse;

public record AuthLoginResponse(
		String accessToken,
		String tokenType,
		UserResponse user
) {
}
