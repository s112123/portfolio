package org.demo.backend.otp.persistence;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.otp.domain.OtpCacheRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Duration;

import static org.demo.backend.global.cache.CaffeineCacheConfig.CacheValue;


@Slf4j
@Profile({"default", "test"})
@Repository
public class OtpCaffeineCacheRepository implements OtpCacheRepository {

    private final Cache<String, CacheValue<?>> otpCache;

    public OtpCaffeineCacheRepository(@Qualifier("otpCache") Cache<String, CacheValue<?>> otpCache) {
        this.otpCache = otpCache;
    }

    @Override
    public void saveOtp(String identifier, String data, Duration ttl) {
        String key = OtpCacheKey.generateOtpKey(identifier);
        otpCache.put(key, new CacheValue<>(data, ttl));
    }

    @Override
    public String getOtp(String identifier) {
        String key = OtpCacheKey.generateOtpKey(identifier);
        return (String) otpCache.getIfPresent(key).getData();
    }

    @Override
    public void invalidateOpt(String identifier) {
        String key = OtpCacheKey.generateOtpKey(identifier);
        otpCache.invalidate(key);
    }

    @Override
    public void saveVerificationResult(String identifier, Duration ttl) {
        String key = OtpCacheKey.generateVerifiedKey(identifier);
        otpCache.put(key, new CacheValue<>("true", ttl));
    }

    @Override
    public boolean isVerified(String identifier) {
        String key = OtpCacheKey.generateVerifiedKey(identifier);
        // 또는 return otpCache.asMap().containsKey(key);
        return otpCache.getIfPresent(key) != null;
    }
}
