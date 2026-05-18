package com.assetmap.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	COMMON_001(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	COMMON_002(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
	COMMON_003(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	CONFIG_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 연동 설정이 올바르지 않습니다."),
	API_AUTH_ERROR(HttpStatus.UNAUTHORIZED, "외부 API 인증에 실패했습니다."),
	AUTH_001(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
	AUTH_002(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	AUTH_003(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
	AUTH_004(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	VALIDATION_001(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
}
