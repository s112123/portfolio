package org.demo.backend.like.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.demo.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PostLikeErrorCode implements ErrorCode {

    ALREADY_LIKES(
            HttpStatus.CONFLICT,
            "ALREADY_LIKES",
            "이미 좋아요를 눌렀습니다"
    ),

    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "LIKES_NOT_FOUND",
            "좋아요를 누른 적이 없습니다"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
