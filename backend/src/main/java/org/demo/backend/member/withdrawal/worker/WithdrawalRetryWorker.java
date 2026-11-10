package org.demo.backend.member.withdrawal.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.member.domain.MemberRole;
import org.demo.backend.member.domain.WithdrawalTaskFailLog;
import org.demo.backend.member.domain.WithdrawalTaskType;
import org.demo.backend.member.persistence.WithdrawalTaskFailLogRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalRetryWorker {

    public static final int MAX_RETRY_COUNT = 5;
    private final PostDeleteWorker postDeleteWorker;
    private final PostLikeDeleteWorker postLikeDeleteWorker;
    private final CommentDeleteWorker commentDeleteWorker;
    private final MemberRepository memberRepository;
    private final WithdrawalTaskFailLogRepository failLogRepository;

    public void process(WithdrawalTaskFailLog failLog) {
        Long memberId = failLog.getMemberId();

        try {
            // 실패한 작업 재시도
            // 1. 현재 작업 타입이 POST_DELETE 라면 실행 후, 다음 단계 타입으로 변경
            if (failLog.getTaskType() == WithdrawalTaskType.POST_DELETE) {
                postDeleteWorker.process(memberId);
                failLog.changeTaskType(WithdrawalTaskType.POST_LIKE_DELETE);
            }

            // 2. 현재 작업 타입이 POST_LIKE_DELETE 라면 실행 후, 다음 단계 타입으로 변경
            if (failLog.getTaskType() == WithdrawalTaskType.POST_LIKE_DELETE) {
                postLikeDeleteWorker.process(memberId);
                failLog.changeTaskType(WithdrawalTaskType.COMMENT_DELETE);
            }

            // 3. 현재 작업 타입이 COMMENT_DELETE 라면 실행
            if (failLog.getTaskType() == WithdrawalTaskType.COMMENT_DELETE) {
                commentDeleteWorker.process(memberId);
                failLog.changeTaskType(WithdrawalTaskType.MEMBER_DELETE);
            }

            // 4. MEMBER_DELETE 재시도 (최종 회원 물리 삭제)
            if (failLog.getTaskType() == WithdrawalTaskType.MEMBER_DELETE) {
                // 회원 삭제
                memberRepository.deleteById(memberId);

                try {
                    // 재시도 성공 시, 실패 로그 삭제
                    failLogRepository.delete(failLog);
                } catch (Exception e) {
                    // 실패한 재시도 작업은 성공했지만 로그만 못 지운 상황이므로 로그만 남긴다
                    // 재시도 비즈니스는 성공했으므로 재시도 횟수를 올리지 않고 다음 스케쥴러에서 delete 만 재시도한다
                    // 다시 실행해도 이미 다 지워진 상태라 안전하게 바로 지워진다
                    log.warn(
                            "회원 탈퇴 재시도 성공 후, 실패 로그 삭제 에러: memberId={}, TaskType={}",
                            memberId,
                            failLog.getTaskType().name()
                    );
                }
            }
        } catch (Exception e) {
            // 재시도 실패 시, 재시도 횟수 + 1 을 한다
            failLog.incrementRetry();
            if (failLog.getRetry() >= MAX_RETRY_COUNT) {
                // 재시도 횟수 초과 데이터는 로그 남기고 개발자에게 알려야 한다
                // 그리고 해당 데이터는 관리자 확용 테이블로 이관하거나 DLQ 로 보내야 한다
                log.error(
                        "[관리자 확인 요망] 회원 탈퇴 재시도 중 횟수 초과: memberId={}, TaskType={}",
                        memberId,
                        failLog.getTaskType().name()
                );
            } else {
                failLogRepository.save(failLog);
            }
        }
    }
}
