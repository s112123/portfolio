package org.demo.backend.member.application.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.like.application.PostLikeService;
import org.demo.backend.like.domain.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeDeleteWorker {

    private final PostLikeService postLikeService;

    public void process(Long memberId) {
        int chunkSize = 100;
        boolean hasNext = true;

        while (hasNext) {
            // Slice<T> 로 받으면 JPQL 작성에서 countQuery 를 하지 않아도 된다
            Page<PostLike> postLikes = postLikeService.findPageByMemberId(memberId, 1, chunkSize);
            if (postLikes.isEmpty()) break;

            List<Long> postIds = postLikes.getContent().stream()
                    .map(postLike -> postLike.getPost().getId())
                    .collect(Collectors.toList());

            // 좋아요 삭제 → 독립적인 트랜잭션에서 실행한다
            // 그런데 지금처럼 반복문으로 처리하면 쿼리가 매우 비효율적이다
            // IN 절로 배치처리하는 것을 권장한다
            for (Long postId : postIds) {
                try {
                    postLikeService.cancelLike(memberId, postId);
                } catch (Exception e) {
                    // 지금처럼 로그만 남기고 스킵할 것이 아니라 실패 상태를 DB 에 기록하고 스케쥴러가 재시도하도록 해야 한다
                    // 미리 회원 상태를 넣어 회원 삭제 시도 시, "삭제 중" 으로 처리해 둠
                    log.error(
                            "[회원 탈퇴] 게시글 삭제 실패: 회원 ID={}, 마지막으로 삭제 성공한 게시물 ID={}",
                            memberId,
                            postId
                    );
                    // return;
                }
            }

            hasNext = postLikes.hasNext();
        }
    }
}
