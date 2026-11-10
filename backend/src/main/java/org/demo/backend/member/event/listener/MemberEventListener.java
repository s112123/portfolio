package org.demo.backend.member.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.member.application.worker.PostDeleteWorker;
import org.demo.backend.member.application.worker.PostLikeDeleteWorker;
import org.demo.backend.member.domain.MemberRepository;
import org.demo.backend.member.event.MemberDeleteEvent;
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
    private final MemberRepository memberRepository;

    // 회원 탈퇴 이벤트
    @Async("memberThreadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteMember(MemberDeleteEvent event) {
        Long memberId = event.getId();
        // 1. 작성한 글 삭제
        postDeleteWorker.process(memberId);
        // 2. 타 게시물의 좋아요 삭제
        postLikeDeleteWorker.process(memberId);
        // 3. 회원 삭제
        memberRepository.deleteById(memberId);
    }
}
