package org.demo.backend.post.persistence.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.post.domain.Post;
import org.demo.backend.post.domain.SortType;
import org.demo.backend.post.domain.cache.PostListCacheKey;
import org.demo.backend.post.domain.cache.PostListCacheRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostListRedisCacheRepository implements PostListCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;
    // 1페이지당 10개씩 3페이지를 캐싱할 것이므로 30개가 캐시되어야 한다
    // 하지만 hasNext 판별을 위해 +1 한다
    private final static long MAX_CACHE_COUNT = 31L;

    @Override
    public void savePostId(SortType sortType, Long postId) {
        // postId == null 이면 Redis 로 네트워크 통신하지 않도록 중단한다
        if (postId == null) return;

        String key = PostListCacheKey.generatePostListKey(sortType);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 글 캐시
        zSetOps.add(key, String.valueOf(postId), postId.doubleValue());
        zSetOps.removeRange(key, 0, -(MAX_CACHE_COUNT + 1));
    }

    @Override
    public void savePostList(SortType sortType, List<Post> posts) {
        if (posts == null || posts.isEmpty()) return;

        String key = PostListCacheKey.generatePostListKey(sortType);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 글 캐시 → 다음과 같은 순회방식이 아닌 파이프라인으로 전달해야 1번만 네트워크 통신을 한다
        for (Post post : posts) {
            zSetOps.add(key, String.valueOf(post.getId()), post.getId().doubleValue());
        }
        zSetOps.removeRange(key, 0, -(MAX_CACHE_COUNT + 1));
    }

    @Override
    public List<Long> findPostList(
            SortType sortType,
            Long lastPostId,
            int size
    ) {
        String key = PostListCacheKey.generatePostListKey(sortType);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 캐시 조회
        // 무한 스크롤 방식은 rank 를 활용한다
        long start = 0;
        if (lastPostId != null) {
            Long rank = zSetOps.reverseRank(key, String.valueOf(lastPostId));
            if (rank == null) {
                return Collections.emptyList();
            }
            start = rank + 1;
        }

        // 최신순 범위 조회
        Set<String> cachedIds = zSetOps.reverseRange(key, start, start + size - 1);
        if (cachedIds == null || cachedIds.isEmpty()) {
            return Collections.emptyList();
        }

        return cachedIds.stream()
                .map(id -> Long.valueOf(id))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPostListCacheBoundary(SortType sortType, Long value) {
        String key = PostListCacheKey.generatePostListKey(sortType);
        // 조회된 값이 마지막 캐시의 값인지 확인
        Set<String> members = redisTemplate.opsForZSet().reverseRange(key, -1, -1);
        return members != null && members.contains(String.valueOf(value));
    }

    @Override
    public void deletePostList(SortType sortType, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return;

        String key = PostListCacheKey.generatePostListKey(sortType);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        Object[] strPostIds = postIds.stream()
                .map(id -> String.valueOf(id))
                .toArray();
        zSetOps.remove(key, strPostIds);
    }
}
