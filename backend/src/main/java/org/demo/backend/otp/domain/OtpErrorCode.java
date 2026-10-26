package org.demo.backend.otp.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.demo.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OtpErrorCode implements ErrorCode {

    INVALID_CODE(
            HttpStatus.BAD_REQUEST,
            "INVALID_CODE",
            "인증 코드가 유효하지 않습니다"
    ),

    NOT_VERIFIED_CODE(
            HttpStatus.BAD_REQUEST,
            "NOT_VERIFIED_CODE",
            "인증이 완료되지 않았습니다"
    ),

    EMAIL_SEND_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "EMAIL_SEND_FAILED",
            "이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요"
    ),

    SMS_SEND_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "SMS_SEND_FAILED",
            "SMS 전송에 실패했습니다. 잠시 후 다시 시도해주세요"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
