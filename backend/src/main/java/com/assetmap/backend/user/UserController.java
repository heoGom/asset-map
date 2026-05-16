package com.assetmap.backend.user;

import com.assetmap.backend.auth.SecurityUtil;
import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final AppUserRepository userRepository;

	public UserController(AppUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/me")
	public ApiResponse<UserResponse> me() {
		AppUser user = userRepository.findById(SecurityUtil.getCurrentUserId())
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));
		return ApiResponse.success(UserResponse.from(user));
	}
}
