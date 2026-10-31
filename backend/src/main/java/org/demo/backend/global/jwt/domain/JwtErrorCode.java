package org.demo.backend.global.jwt.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.demo.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum JwtErrorCode implements ErrorCode {

    NOT_FOUND(
            HttpStatus.UNAUTHORIZED,
            "NOT_FOUND_TOKEN",
            "로그인이 필요한 서비스입니다"
    ),

    INVALID(
            HttpStatus.UNAUTHORIZED,
            "INVALID_TOKEN",
            "인증이 유효하지 않습니다"
    ),

    EXPIRED(
            HttpStatus.UNAUTHORIZED,
            "EXPIRED_TOKEN",
            "만료된 토큰입니다"
    ),

    MALFORMED(
            HttpStatus.UNAUTHORIZED,
            "MALFORMED_TOKEN",
            "손상되거나 잘못된 형식의 토큰입니다"
    ),

    INVALID_SIGNATURE(
            HttpStatus.UNAUTHORIZED,
            "INVALID_SIGNATURE",
            "토큰 서명이 위변조되었거나 일치하지 않습니다"
    ),

    UNSUPPORTED(
            HttpStatus.UNAUTHORIZED,
            "UNSUPPORTED_TOKEN",
            "지원하지 않는 형식의 토큰입니다"
    ),

    ILLEGAL_ARGUMENT(
            HttpStatus.UNAUTHORIZED,
            "ILLEGAL_ARGUMENT_TOKEN",
            "토큰 값이 비어있거나 잘못되었습니다"
    ),

    TOKEN_THEFT_DETECTED(
            HttpStatus.UNAUTHORIZED,
            "TOKEN_THEFT_DETECTED",
            "보안상 위험이 감지되어 강제 로그아웃되었습니다. 다시 로그인해주세요."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
