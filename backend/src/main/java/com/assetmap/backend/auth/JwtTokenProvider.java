package com.assetmap.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final SecretKey secretKey;
	private final long expirationMs;

	public JwtTokenProvider(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String createToken(Long userId, String email) {
		Date now = new Date();
		Date expiresAt = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(email)
				.claim("userId", userId)
				.issuedAt(now)
				.expiration(expiresAt)
				.signWith(secretKey)
				.compact();
	}

	public String getEmail(String token) {
		return claims(token).getSubject();
	}

	public boolean validate(String token) {
		claims(token);
		return true;
	}

	private Claims claims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
