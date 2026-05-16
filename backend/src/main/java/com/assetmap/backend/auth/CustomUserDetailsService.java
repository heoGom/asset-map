package com.assetmap.backend.auth;

import com.assetmap.backend.user.AppUser;
import com.assetmap.backend.user.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final AppUserRepository userRepository;

	public CustomUserDetailsService(AppUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		AppUser user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException(email));
		return new UserPrincipal(user);
	}
}
