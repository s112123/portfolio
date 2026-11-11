package org.demo.backend.post.persistence.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.post.application.dto.PostDetail;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.demo.backend.post.domain.SortType.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostListRedisCacheRepository implements PostListCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;
    // 1페이지당 10개씩 3페이지를 캐싱할 것이므로 30개가 캐시되어야 한다
    public static final int MAX_CACHE_COUNT = 31;

    @Override
    public void savePostList(SortType sortType, List<PostDetail> posts) {
        // postId == null 이면 Redis 로 네트워크 통신하지 않도록 중단한다
        if (posts == null || posts.isEmpty()) return;

        String key = PostListCacheKey.generatePostListKey(sortType);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 캐시 저장
        // 원래는 다음과 같은 순회방식이 아닌 파이프라인으로 전달해야 1번만 네트워크 통신해야 한다
        for (PostDetail post : posts) {
            // 정렬별 스코어
            double score = 0.0;
            switch (sortType) {
                case LATEST:
                    score = post.getId().doubleValue();
                    break;
                case LIKES:
                    // 좋아요가 동점인 경우, 두 번째 정렬 기준으로 ID 를 소수점으로 만들어 비교한다
                    // postId=3, likes=10 → 10.0000003
                    // postId=2, likes=10 → 10.0000002
                    // 단, 이 방법은 ID 가 10000000개가 되면 post.getLikes() + 1.0 이 되므로 주의한다
                    // 참고) 해결 방안 → score 에는 좋아요 수만 저장하고 member 를 정렬한다
                    // score = post.getHits();
                    // String member = String.format("%019d", post.getId()); → 문자열 숫자가 정렬이 똑바로 되도록 처리
                    // zSetOps.add(key, member, score);
                    score = post.getLikes() + (post.getId() / 10000000.0);
                    break;
                case HITS:
                    // 조회수가 동점인 경우, 두 번째 정렬 기준으로 ID 를 소수점으로 만들어 비교한다
                    score = post.getHits() + (post.getId() / 10000000.0);
                    break;
            }

            // 캐시 저장
            zSetOps.add(key, String.valueOf(post.getId()), score);

            // MAX_CACHE_COUNT 개수만큼만 저장
            // 조회할 때, 내림차순으로 정렬하기 때문에 낮은 순위인 앞쪽을 제거한다
            // 참고) removeRange(key, 0, -(MAX_CACHE_COUNT + 1)); 는 인덱스 보정이 발생하여 예상치 못한 삭제 발생할 수 있다
            Long cacheCount = zSetOps.zCard(key);
            if (cacheCount != null && cacheCount > MAX_CACHE_COUNT) {
                long removeCount = cacheCount - MAX_CACHE_COUNT;
                zSetOps.removeRange(key, 0, removeCount - 1);
            }
        }

        // TTL (현재 좋아요와 조회수는 갱신될 때마다 무효화하고 있으므로 TTL 은 비정상 캐시에 대한 안전장치이다)
        redisTemplate.expire(key, 6, TimeUnit.HOURS);
    }

    @Override
    public List<Long> findPostList(SortType sortType, Long lastPostId, int size) {
        String key = PostListCacheKey.generatePostListKey(sortType);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 첫 페이지인 경우
        // 1페이지당 10개이지만 hasNext 판별을 위해 11개를 조회한다
        if (lastPostId == null) {
            Set<String> postIds = zSetOps.reverseRange(key, 0, size);
            return postIds.stream()
                    .map(postId -> Long.valueOf(postId))
                    .collect(Collectors.toList());
        }

        // 첫 페이지가 아닌 경우
        // ZRANGE 또는 ZREVRANGE 는 순위를 기준으로 일정 범위를 조회한다
        // 무한 스크롤의 경우, 마지막 ID 의 순위를 구해서 그 순위부터 size 만큼 조회한다
        Long rank = zSetOps.reverseRank(key, String.valueOf(lastPostId));
        if (rank == null) {
            return Collections.emptyList();
        }

        // 1페이지당 10개이지만 hasNext 판별을 위해 11개를 조회한다
        Set<String> postIds = zSetOps.reverseRange(key, rank + 1, rank + size + 1);
        return postIds.stream()
                .map(postId -> Long.valueOf(postId))
                .collect(Collectors.toList());
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

    @Override
    public void deleteKey(SortType sortType) {
        String key = PostListCacheKey.generatePostListKey(sortType);
        redisTemplate.delete(key);
    }
}
