package com.assetmap.backend.common.exception;

import com.assetmap.backend.common.response.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.error(errorCode));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<List<ValidationError>>> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException exception
	) {
		List<ValidationError> errors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(ValidationError::from)
				.toList();

		return ResponseEntity
				.status(ErrorCode.VALIDATION_001.getStatus())
				.body(ApiResponse.error(ErrorCode.VALIDATION_001, errors));
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
		return ResponseEntity
				.status(ErrorCode.COMMON_001.getStatus())
				.body(ApiResponse.error(ErrorCode.COMMON_001));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		return ResponseEntity
				.status(ErrorCode.COMMON_003.getStatus())
				.body(ApiResponse.error(ErrorCode.COMMON_003));
	}

	public record ValidationError(
			String field,
			String message
	) {
		private static ValidationError from(FieldError fieldError) {
			return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
		}
	}
}
