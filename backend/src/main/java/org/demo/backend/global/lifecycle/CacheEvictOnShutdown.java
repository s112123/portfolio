package org.demo.backend.global.lifecycle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictOnShutdown {

    private final RedisTemplate<String, String> redisTemplate;

    @PreDestroy
    public void clearRedisCache() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            connection.serverCommands().flushDb();
            log.info("[Shutdown] Redis 캐시 초기화 완료");
        } catch (Exception e) {
            log.error("[Shutdown] Redis 캐시 초기화 실패: {}", e.getMessage());
        }
    }
}
