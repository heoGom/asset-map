package com.assetmap.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthSignupRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8) String password,
		@NotBlank String nickname
) {
}
