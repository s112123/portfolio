package org.demo.backend.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.demo.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements ErrorCode {

    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "POST_NOT_FOUND",
            "존재하지 않는 글입니다"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
