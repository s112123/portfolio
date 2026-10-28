package org.demo.backend.global.lock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.demo.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LockErrorCode implements ErrorCode {

    CURRENT_REQUEST_BLOCKED(
            HttpStatus.CONFLICT, // 또는 429 TOO_MANY_REQUEST
            "CURRENT_REQUEST_BLOCKED",
            "이미 요청을 처리 중입니다"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
