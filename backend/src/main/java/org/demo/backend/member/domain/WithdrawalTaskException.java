package org.demo.backend.member.domain;

import lombok.Getter;

@Getter
public class WithdrawalTaskException extends RuntimeException {

    private final WithdrawalTaskType taskType;
    private final Long memberId;

    public WithdrawalTaskException(WithdrawalTaskType taskType, Long memberId, Throwable cause) {
        super("회원 탈퇴 작업 실패: " + taskType, cause);
        this.taskType = taskType;
        this.memberId = memberId;
    }
}
