package org.demo.backend.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.demo.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProfileImageErrorCode implements ErrorCode {

    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PROFILE_IMAGE_NOT_FOUND",
            "프로필 이미지 파일이 존재하지 않습니다"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
