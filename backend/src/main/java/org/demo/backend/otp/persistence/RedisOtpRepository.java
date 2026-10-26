package org.demo.backend.otp.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.cache.CaffeineCacheConfig;
import org.demo.backend.otp.domain.OtpRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Profile({"dev", "prod"})
@Repository
@RequiredArgsConstructor
public class RedisOtpRepository implements OtpRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveOtp(String identifier, String data, Duration ttl) {
        String key = OtpCacheKey.generateOtpKey(identifier);
        redisTemplate.opsForValue().set(key, data, ttl);
    }

    @Override
    public String getOtp(String identifier) {
        String key = OtpCacheKey.generateOtpKey(identifier);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void invalidateOpt(String identifier) {
        String key = OtpCacheKey.generateOtpKey(identifier);
        redisTemplate.delete(key);
    }

    @Override
    public void saveVerificationResult(String identifier, Duration ttl) {
        String key = OtpCacheKey.generateOtpKey(identifier);
        redisTemplate.opsForValue().set(key, "true", ttl);
    }

    @Override
    public boolean isVerified(String identifier) {
        String key = OtpCacheKey.generateOtpKey(identifier);
        return redisTemplate.hasKey(key);
    }
}
