package org.demo.backend.post.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.post.event.PostHitsChangeEvent;
import org.demo.backend.post.event.PostLikesChangeEvent;
import org.demo.backend.post.domain.SortType;
import org.demo.backend.post.domain.cache.PostHitsCacheRepository;
import org.demo.backend.post.domain.cache.PostListCacheRepository;
import org.demo.backend.post.event.PostDeleteEvent;
import org.demo.backend.post.event.PostCreateEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;
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
    public void createPostListCache(PostCreateEvent event) {
        try {
            postListCacheRepository.savePostList(SortType.LATEST, Arrays.asList(event.getPostDetail()));
            postListCacheRepository.savePostList(SortType.LIKES, Arrays.asList(event.getPostDetail()));

            // 조회순 스케쥴러에서 캐시를 무효화하는데 그 후, 글이 등록되면 등록된 글만 캐시되어 조회된다
            // 그래서 조회순 캐시는 무효화시킨다
            postListCacheRepository.deleteKey(SortType.HITS);
        } catch (Exception e) {
            log.warn("글 등록 후, 캐시 등록 실패: {}", event.getPostDetail().getId(), e);
        }
    }

    // 좋아요 수 변경 시, 좋아요 글 목록 캐시 무효화
    @Async("postThreadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updatePostLikesCache(PostLikesChangeEvent event) {
        try {
            // 단순히 캐시된 특정 글의 좋아요 수만 바꿨을 때, 캐시에 없는 글의 좋아요가 급상승하여도 캐시에 들지 못한다
            // 그래서 트래픽이 적고 좋아요 수 변경이 적다면 무효화시키는 방법이 간단하다
            postListCacheRepository.deleteKey(SortType.LIKES);
        } catch (Exception e) {
            log.warn("좋아요 수 변경 후, 캐시 무효화 실패", e);
        }
    }

    // 조회 수 변경 시, 조회순 글 목록 캐시 무효화
    @Async("postThreadPoolTaskExecutor")
    @EventListener
    public void updatePostHitsCache(PostHitsChangeEvent event) {
        try {
            postListCacheRepository.deleteKey(SortType.HITS);
        } catch (Exception e) {
            log.warn("조회 수 변경 후, 캐시 무효화 실패", e);
        }
    }

    // 삭제된 글의 캐시 삭제 이벤트
    @Async("postThreadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void clearDeletedPostCache(PostDeleteEvent event) {
        for (Long postId : event.getPostIds()) {
            try {
                // 조회 수 캐시 삭제 → 원자적으로 처리하는 것을 권장한다
                postHitsCacheRepository.deleteHits(postId);
                postHitsCacheRepository.deleteViewer(postId);

                // 글 목록 (최신순) 캐시 삭제
                postListCacheRepository.deletePostList(
                        SortType.LATEST,
                        Collections.singletonList(postId)
                );
                // 글 목록 (좋아요순) 캐시 삭제
                postListCacheRepository.deleteKey(SortType.LIKES);
                // 글 목록 (조회순) 캐시 삭제
                postListCacheRepository.deleteKey(SortType.HITS);
            } catch (Exception e) {
                log.warn("글 삭제 후, 캐시 정리에 실패했습니다: postId={}", postId, e);
            }
        }
    }
}
