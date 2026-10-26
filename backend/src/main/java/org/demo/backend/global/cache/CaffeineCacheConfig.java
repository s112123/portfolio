package org.demo.backend.global.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    // Caffeine 은 Redis 처럼 TTL 을 함께 저장하는 메서드를 제공하지 않는다
    // 그래서 동적 TTL 을 구성하려면 다음과 같이 한다
    @Qualifier("otpCache")
    @Bean
    public Cache<String, OtpCacheValue> otpCache() {
        return Caffeine.newBuilder()
                .expireAfter(new Expiry<String, OtpCacheValue>() {
                    @Override
                    public long expireAfterCreate(
                            String key,
                            OtpCacheValue value,
                            long currentTime
                    ) {
                        // 래퍼 객체에 담긴 TTL 을 읽어와서 나노초로 변환 (Caffeine 요구 규격)
                        return value.getTtl().toNanos();
                    }

                    @Override
                    public long expireAfterUpdate(
                            String key,
                            OtpCacheValue value,
                            long currentTime,
                            long currentDuration
                    ) {
                        return value.getTtl().toNanos();
                    }

                    @Override
                    public long expireAfterRead(
                            String key,
                            OtpCacheValue value,
                            long currentTime,
                            long currentDuration
                    ) {
                        // 조회 시에는 기존 만료 시간 유지
                        return currentDuration;
                    }
                })
                .build();
    }

    // 값과 TTL 을 함께 담는 래퍼 클래스
    @Getter
    @AllArgsConstructor
    public static class OtpCacheValue {

        private final String data;
        private final Duration ttl;
    }
}
