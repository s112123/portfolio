package org.demo.backend.member.withdrawal.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.member.domain.WithdrawalTaskException;
import org.demo.backend.member.domain.WithdrawalTaskFailLog;
import org.demo.backend.member.domain.WithdrawalTaskType;
import org.demo.backend.member.persistence.WithdrawalTaskFailLogRepository;
import org.demo.backend.member.withdrawal.worker.CommentDeleteWorker;
import org.demo.backend.member.withdrawal.worker.PostDeleteWorker;
import org.demo.backend.member.withdrawal.worker.PostLikeDeleteWorker;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.member.withdrawal.event.MemberDeleteEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberEventListener {

    private final PostDeleteWorker postDeleteWorker;
    private final PostLikeDeleteWorker postLikeDeleteWorker;
    private final CommentDeleteWorker commentDeleteWorker;
    private final MemberRepository memberRepository;
    private final WithdrawalTaskFailLogRepository failLogRepository;

    // 회원 탈퇴 이벤트
    @Async("memberThreadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteMember(MemberDeleteEvent event) {
        Long memberId = event.getId();

        try {
            // 1. 작성한 글 삭제
            postDeleteWorker.process(memberId);
            // 2. 타 게시물의 좋아요 삭제
            postLikeDeleteWorker.process(memberId);
            // 3. 타 게시물의 댓글 삭제
            commentDeleteWorker.process(memberId);

            // 4. 회원 삭제
            try {
                memberRepository.deleteById(memberId);
            } catch (Exception e) {
                throw new WithdrawalTaskException(WithdrawalTaskType.MEMBER_DELETE, memberId, e);
            }
        } catch (WithdrawalTaskException we) {
            // 실패 시, 실패 로그를 DB 에 저장
            try {
                failLogRepository.save(WithdrawalTaskFailLog.of(we.getTaskType(), we.getMemberId()));
            } catch (Exception e) {
                // DB 문제로 개발자 개입이 필요한 상황으로 개발자에게 알림을 보낸다
                log.error("[관리자 확인 요망][회원 탈퇴] 실패 로그 저장 중 에러: 회원 ID={}", memberId, e);
            }
        }
    }
}
