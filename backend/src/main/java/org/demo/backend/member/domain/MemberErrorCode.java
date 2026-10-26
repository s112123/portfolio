package org.demo.backend.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.demo.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    DUPLICATED_EMAIL(
            HttpStatus.CONFLICT,
            "DUPLICATED_EMAIL",
            "이미 존재하는 이메일입니다"
    ),

    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MEMBER_NOT_FOUND",
            "존재하지 않는 회원입니다"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
