package org.demo.backend.post.persistence.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.post.domain.cache.PostHitsCacheKey;
import org.demo.backend.post.domain.cache.PostHitsCacheRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostHitsRedisCacheRepository implements PostHitsCacheRepository {

    // Redis 에서 EXPIRE 는 초 단위이다
    // 24시간 → 86400초
    // 5분 → 86400초
    private static final Duration POST_VIEWERS_KEY_TTL = Duration.ofSeconds(300);
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Long addViewerAndIncrementDelta(Long postId, String viewer) {
        String viewerKey = PostHitsCacheKey.generatePostViewerKey(postId);
        String hitsDeltaKey = PostHitsCacheKey.generatePostHitsKey();

        // Lua Script
        // 게시물을 확인한 사용자를 SET 에 등록하고, 중복 조회 체크 후, 각 게시물의 조회 수 증분값을 HASH 에 저장한다
        // KEYS[1] → post:viewers:{postId}
        // KEYS[2] → post:hits:delta
        // ARGV[1] → 사용자 식별자
        // ARGV[2] → SET 의 TTL
        // ARGV[3] → HASH 의 필드
        RedisScript<Long> VIEW_COUNT_SCRIPT = RedisScript.of(
                "local added = redis.call('SADD', KEYS[1], ARGV[1]) " +
                        "if added == 1 then " +
                        "   redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
                        "   redis.call('HINCRBY', KEYS[2], ARGV[3], 1) " +
                        "end " +
                        "local currentDelta = redis.call('HGET', KEYS[2], ARGV[3]) " +
                        "if not currentDelta then " +
                        "   return 0 " +
                        "end " +
                        "return tonumber(currentDelta)",
                Long.class
        );
        Long delta = redisTemplate.execute(
                VIEW_COUNT_SCRIPT,
                Arrays.asList(viewerKey, hitsDeltaKey),
                viewer,
                String.valueOf(POST_VIEWERS_KEY_TTL.getSeconds()),
                String.valueOf(postId)
        );
        return delta != null ? delta : 0L;
    }

    // 아래 방식은 대규모 처리에서 메모리 문제를 고려해야 한다
    // Redis 에서 수만 캐시를 HGETALL 과 DEL 을 처리하면 싱글 쓰레드이므로 일시적으로 Blocking 될 수 있다
    // 수만 캐시의 데이터를 조회하여 Map 에 담으면 JVM 힙 메모리가 OutOfMemoryError 가 발생할 수 있다
    // 이 경우, 보통 스케쥴러 주기 (1~3분) 를 짧게하여 적은 데이터 양을 자주 처리하도록 한다
    // 또는 HASH 를 일정 크기로 나누어 처리하거나 HSCAN 조회 등 청크 방식을 고려한다
    // 그런데 HSCAN 방식도 1번 청크를 읽고 2번 청크를 읽는 틈에 조회 수 증분값이 또 쌓일 수 있어 구현이 복잡하다
    @Override
    public Map<Long, Long> getAllHitsDeltaAndClear() {
        String key = PostHitsCacheKey.generatePostHitsKey();

        // Lua Script
        // 조회 수 증분값을 읽고 (HGETALL), 읽은 필드를 삭제 (DEL) 해야 한다
        // 하지만 두 메서드로 분리해서 동작하면 증분 값을 읽고 필드를 삭제하는 사이에 캐시된 증분값은 유실될 수 있다
        // 그래서 원자적으로 처리한다
        // 단, 원자적으로 DEL 까지 실행된 후, DB 반영 중에 실패하면 증가분은 유실될 수 있는 점을 생각해야 한다
        String GET_HITS_AND_CLEAR_SCRIPT =
                "local result = redis.call('HGETALL', KEYS[1]) " +
                "redis.call('DEL', KEYS[1]) " +
                "return result";
        DefaultRedisScript<List> redisScript = getRedisScript(GET_HITS_AND_CLEAR_SCRIPT, List.class);
        List<Object> rawResult = redisTemplate.execute(
                redisScript,
                Collections.singletonList(key)
        );

        if (rawResult == null || rawResult.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Long> hitsDeltaMap = new HashMap<>();
        for (int i = 0; i < rawResult.size(); i += 2) {
            Long postId = Long.valueOf((String) rawResult.get(i));
            Long delta = Long.valueOf((String) rawResult.get(i + 1));
            hitsDeltaMap.put(postId, delta);
        }
        return hitsDeltaMap;
    }

    @Override
    public void restoreHitsDelta(Map<Long, Long> postHitsDelta) {
        if (postHitsDelta == null || postHitsDelta.isEmpty()) {
            return;
        }
        String key = PostHitsCacheKey.generatePostHitsKey();

        // 기존 값에 실패한 delta 만큼 더해준다
        // 그런데 아래 방식은 increment() 가 실행될 때마다 네트워크 통신이 발생한다
        // 그래서 Pipeline 을 사용하는 것을 권장한다 → executePipelined()
        for (Map.Entry<Long, Long> entry : postHitsDelta.entrySet()) {
            String field = String.valueOf(entry.getKey());
            Long delta = entry.getValue();
            redisTemplate.opsForHash().increment(key, field, delta);
        }
    }

    private <T> DefaultRedisScript<T> getRedisScript(String script, Class<T> resultType) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(resultType);
        return redisScript;
    }
}
