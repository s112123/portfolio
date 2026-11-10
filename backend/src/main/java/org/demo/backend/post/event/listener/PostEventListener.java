package org.demo.backend.post.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.post.domain.SortType;
import org.demo.backend.post.domain.cache.PostHitsCacheRepository;
import org.demo.backend.post.domain.cache.PostListCacheRepository;
import org.demo.backend.post.event.PostCacheDeleteEvent;
import org.demo.backend.post.event.PostCreateEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final PostListCacheRepository postListCacheRepository;
    private final PostHitsCacheRepository postHitsCacheRepository;

    // 등록한 글을 캐시한다 → 글 목록 조회를 위한 캐시
    @Async("postThreadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cacheCreatedPost(PostCreateEvent event) {
        try {
            postListCacheRepository.savePostId(SortType.LATEST, event.getPostId());
        } catch (Exception e) {
            log.warn("글 등록 후, 캐시 등록에 실패했습니다: {}", event.getPostId(), e);
        }
    }

    // 삭제된 글의 캐시 삭제 이벤트
    @Async("postThreadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void clearDeletedPostCache(PostCacheDeleteEvent event) {
        for (Long postId : event.getPostIds()) {
            try {
                // 조회 수 캐시 삭제
                postHitsCacheRepository.deleteHits(postId);
                postHitsCacheRepository.deleteViewer(postId);

                // 글 목록 캐시 삭제
                postListCacheRepository.deletePostList(
                        SortType.LATEST,
                        Collections.singletonList(postId)
                );
            } catch (Exception e) {
                log.warn("글 삭제 후, 캐시 정리에 실패했습니다: postId={}", postId, e);
            }
        }
    }
}
