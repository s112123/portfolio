package org.demo.backend.global.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CaffeineCacheConfig {

    // Caffeine 은 Redis 처럼 TTL 을 함께 저장하는 메서드를 제공하지 않는다
    // 그래서 동적 TTL 을 구성하려면 다음과 같이 한다
    @Qualifier("otpCache")
    @Bean
    public Cache<String, CacheValue<?>> otpCache() {
        return Caffeine.newBuilder()
                .expireAfter(createExpiryPolicy())
                .build();
    }

    @Qualifier("jwtCache")
    @Bean
    public Cache<String, CacheValue<?>> jwtCache() {
        return Caffeine.newBuilder()
                .expireAfter(createExpiryPolicy())
                .build();
    }

    // Expiry 정책 생성
    private Expiry<String, CacheValue<?>> createExpiryPolicy() {
        return new Expiry<String, CacheValue<?>>() {
            @Override
            public long expireAfterCreate(String key, CacheValue<?> value, long currentTime) {
                // 래퍼 객체에 담긴 TTL 을 읽어와서 나노초로 변환 (Caffeine 요구 규격)
                return value.getTtl().toNanos();
            }

            @Override
            public long expireAfterUpdate(String key, CacheValue<?> value, long currentTime, long currentDuration) {
                return value.getTtl().toNanos();
            }

            @Override
            public long expireAfterRead(String key, CacheValue<?> value, long currentTime, long currentDuration) {
                // 조회 시에는 기존 만료 시간 유지
                return currentDuration;
            }
        };
    }

    // 값과 TTL 을 함께 담는 래퍼 클래스
    @Getter
    @AllArgsConstructor
    public static class CacheValue<T> {

        private final T data;
        private final Duration ttl;
    }
}
