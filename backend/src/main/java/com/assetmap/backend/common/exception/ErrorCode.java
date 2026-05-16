package com.assetmap.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	COMMON_001(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	COMMON_002(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
	COMMON_003(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
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
