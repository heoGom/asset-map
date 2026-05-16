package com.assetmap.backend.common.response;

import com.assetmap.backend.common.exception.ErrorCode;

public record ApiResponse<T>(
		boolean success,
		String code,
		String message,
		T data
) {

	private static final String SUCCESS_CODE = "SUCCESS";
	private static final String SUCCESS_MESSAGE = "요청이 성공했습니다.";

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, SUCCESS_CODE, SUCCESS_MESSAGE, data);
	}

	public static ApiResponse<Void> successWithoutData() {
		return success(null);
	}

	public static ApiResponse<Void> error(ErrorCode errorCode) {
		return error(errorCode, null);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
		return new ApiResponse<>(false, errorCode.name(), errorCode.getMessage(), data);
	}
}
