package org.demo.backend.global.jwt.persistence;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.jwt.application.dto.JwtDetail;
import org.demo.backend.global.jwt.domain.JwtCacheRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Duration;

import static org.demo.backend.global.cache.CaffeineCacheConfig.CacheValue;

@Slf4j
@Profile({"default", "test"})
@Repository
public class JwtCaffeineCacheRepository implements JwtCacheRepository<JwtDetail> {

    private final Cache<String, CacheValue<?>> jwtCache;

    public JwtCaffeineCacheRepository(@Qualifier("jwtCache") Cache<String, CacheValue<?>> jwtCache) {
        this.jwtCache = jwtCache;
    }

    @Override
    public void saveToken(String identifier, JwtDetail value, Duration ttl) {
        String key = JwtCacheKey.generateJwtKey(identifier);
        jwtCache.put(key, new CacheValue<>(value, ttl));
    }

    @Override
    public JwtDetail getToken(String identifier) {
        String key = JwtCacheKey.generateJwtKey(identifier);
        return (JwtDetail) jwtCache.getIfPresent(key).getData();
    }

    @Override
    public void invalidateToken(String identifier) {
        String key = JwtCacheKey.generateJwtKey(identifier);
        jwtCache.invalidate(key);
    }

    @Override
    public void saveBlacklistToken(String identifier, JwtDetail value, Duration ttl) {
        String key = JwtCacheKey.generateBlacklistKey(identifier);
        jwtCache.put(key, new CacheValue<>(value, ttl));
    }

    @Override
    public JwtDetail getBlacklistToken(String identifier) {
        String key = JwtCacheKey.generateBlacklistKey(identifier);
        return (JwtDetail) jwtCache.getIfPresent(key).getData();
    }

    @Override
    public boolean isBlacklisted(String identifier) {
        String key = JwtCacheKey.generateBlacklistKey(identifier);
        return jwtCache.getIfPresent(key) != null;
    }
}
