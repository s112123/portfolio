package org.demo.backend.member.withdrawal.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.web.dto.CursorDetail;
import org.demo.backend.member.domain.WithdrawalTaskException;
import org.demo.backend.member.domain.WithdrawalTaskType;
import org.demo.backend.post.application.PostService;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.PostRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostDeleteWorker {

    private final PostRepository postRepository;
    private final PostService postService;

    public void process(Long memberId) {
        Long lastPostId = null;
        int chunkSize = 100;

        while (true) {
            // 1. 100개씩 글 조회
            CursorDetail<Post> cursoredPosts = postRepository.findCursorByMemberIdAndIdLessThan(
                    memberId,
                    lastPostId,
                    chunkSize
            );
            List<Post> posts = cursoredPosts.getContent();
            if (posts.isEmpty()) break;

            // 2. 글 삭제 → 청크별로 독립적인 트랜잭션에서 실행한다
            // 독립적인 트랜잭션에서 실행하기 때문에 while 문 안에서 try-catch 를 사용한다
            try {
                List<Long> deletedPostIds = posts.stream()
                        .map(post -> post.getId())
                        .collect(Collectors.toList());
                postService.deleteByIdIn(deletedPostIds);
            } catch (Exception e) {
                // 지금처럼 로그만 남기고 스킵할 것이 아니라 실패 상태를 DB 에 기록하고 스케쥴러가 재시도하도록 해야 한다
                // 미리 회원 상태를 넣어 회원 삭제 시도 시, "삭제 중" 으로 처리해 둠
                log.error(
                        "[회원 탈퇴] 게시글 삭제 실패: 회원 ID={}, 삭제를 실패한 게시글 ID={}",
                        memberId,
                        posts.get(0).getId()
                );
                // 실패한 시점에는 워커 작업을 중단한다
                // 만약, 스케쥴러에서 실패한 작업을 재시도할 때 중복 처리로 꼬일 수 있기 때문이다
                throw new WithdrawalTaskException(WithdrawalTaskType.POST_DELETE, memberId, e);
            }

            // 3. 조회된 글이 청크 사이즈보다 작으면 마지막 페이지이므로 반복 종료하고 아니면 커서 갱신
            if (posts.size() < chunkSize) break;
            lastPostId = posts.get(posts.size() - 1).getId();
        }
    }
}
