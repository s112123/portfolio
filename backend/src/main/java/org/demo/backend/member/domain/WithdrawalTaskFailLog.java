package org.demo.backend.member.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.backend.global.jpa.BaseTimeEntity;

import javax.persistence.*;

@Entity
@Table(name = "delete_fail_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalTaskFailLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalTaskType taskType;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private int retry;

    public static WithdrawalTaskFailLog of(
            WithdrawalTaskType taskType,
            Long memberId
    ) {
        WithdrawalTaskFailLog failLog = new WithdrawalTaskFailLog();
        failLog.taskType  = taskType;
        failLog.memberId = memberId;
        failLog.retry = 0;
        return failLog;
    }

    public void incrementRetry() {
        this.retry += 1;
    }

    public void changeTaskType(WithdrawalTaskType taskType) {
        this.taskType = taskType;
    }
}
