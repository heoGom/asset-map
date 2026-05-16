package com.assetmap.backend.auth;

import com.assetmap.backend.common.exception.BusinessException;
import com.assetmap.backend.common.exception.ErrorCode;
import com.assetmap.backend.user.AppUser;
import com.assetmap.backend.user.AppUserRepository;
import com.assetmap.backend.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

	private final AppUserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(
			AppUserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Transactional
	public AuthLoginResponse signup(AuthSignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.AUTH_003);
		}
		AppUser user = userRepository.save(new AppUser(
				request.email(),
				passwordEncoder.encode(request.password()),
				request.nickname()
		));
		return tokenResponse(user);
	}

	public AuthLoginResponse login(AuthLoginRequest request) {
		AppUser user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_002));
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(ErrorCode.AUTH_002);
		}
		return tokenResponse(user);
	}

	private AuthLoginResponse tokenResponse(AppUser user) {
		return new AuthLoginResponse(
				jwtTokenProvider.createToken(user.getId(), user.getEmail()),
				"Bearer",
				UserResponse.from(user)
		);
	}
}
