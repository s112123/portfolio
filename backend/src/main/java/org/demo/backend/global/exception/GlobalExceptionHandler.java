package org.demo.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.exception.dto.ErrorDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 컨트롤러에서 요청 DTO 의 Validation 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<String> errorMessages = fieldErrors.stream()
                .map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(new ErrorDetail<>(status, errorMessages));
    }

    // 잘못된 인자 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetail<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(new ErrorDetail<>(status, e.getMessage()));
    }

    // 비즈니스 로직 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDetail<?>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorDetail<>(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage()));
    }

    // 예측할 수 없는 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail<?>> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(new ErrorDetail<>(status, "서버 내부 오류가 발생했습니다"));
    }
}
