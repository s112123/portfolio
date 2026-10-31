package org.demo.backend.global.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.exception.dto.ErrorDetail;
import org.demo.backend.global.jwt.domain.JwtErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
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

    // 파일 업로드 용량 초과 예외
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorDetail<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(new ErrorDetail<>(status, "첨부 파일 크기는 1MB 이하이어야 합니다"));
    }

    // 인증 예외
    // 원래 시큐리티의 예외는 @RestControllerAdvice 에서 잡을 수 없다
    // 하지만 REST API 로 로그인하는 경우, 시큐리티 필터를 통과하여 DispatcherServlet 으로 넘어오기 때문에 잡을 수 있다
    // 단, 보안의 목적으로 스프링 내부에서 BadCredentialsException 예외로 감싸서 터뜨린다
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetail<?>> handleBadCredentialsException(BadCredentialsException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity
                .status(status)
                .body(new ErrorDetail<>(status, "이메일 또는 비밀번호가 올바르지 않습니다"));
    }

    // JWT 예외
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorDetail<?>> handleJwtException(JwtException e) {
        ErrorCode errorCode = JwtErrorCode.INVALID;
        if (e instanceof ExpiredJwtException) {
            errorCode = JwtErrorCode.EXPIRED;
        } else if (e instanceof MalformedJwtException) {
            errorCode = JwtErrorCode.MALFORMED;
        } else if (e instanceof SignatureException) {
            errorCode = JwtErrorCode.INVALID_SIGNATURE;
        } else if (e instanceof UnsupportedJwtException) {
            errorCode = JwtErrorCode.UNSUPPORTED;
        }

        HttpStatus status = errorCode.getStatus();
        return ResponseEntity
                .status(status)
                .body(new ErrorDetail<>(status, errorCode.getCode(), errorCode.getMessage()));
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
    public ResponseEntity<ErrorDetail<?>> handleException(Exception e, HttpServletRequest request) {
        log.error("[Unexpected Error] URI: {}, Method: {}, Message: {}",
                request.getRequestURI(),
                request.getMethod(),
                e.getMessage(),
                e
        );
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(new ErrorDetail<>(status, "서버 내부 오류가 발생했습니다"));
    }
}
