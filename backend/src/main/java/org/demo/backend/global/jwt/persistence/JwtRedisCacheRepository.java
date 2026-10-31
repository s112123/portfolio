package org.demo.backend.global.jwt.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.jwt.application.dto.JwtDetail;
import org.demo.backend.global.jwt.domain.JwtCacheRepository;
import org.demo.backend.global.serde.DataSerializer;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Profile({"dev", "prod"})
@Repository
@RequiredArgsConstructor
public class JwtRedisCacheRepository implements JwtCacheRepository<JwtDetail> {

    private final RedisTemplate<String, String> redisTemplate;
    private final DataSerializer dataSerializer;

    @Override
    public void saveToken(String identifier, JwtDetail value, Duration ttl) {
        String key = JwtCacheKey.generateJwtKey(identifier);
        redisTemplate.opsForValue().set(key, dataSerializer.serializeOrException(value), ttl);
    }

    @Override
    public JwtDetail getToken(String identifier) {
        String key = JwtCacheKey.generateJwtKey(identifier);
        String jwtDetail = redisTemplate.opsForValue().get(key);
        return dataSerializer.deserializeOrNull(jwtDetail, JwtDetail.class);
    }

    @Override
    public void invalidateToken(String identifier) {
        String key = JwtCacheKey.generateJwtKey(identifier);
        redisTemplate.delete(key);
    }

    @Override
    public void saveBlacklistToken(String identifier, JwtDetail value, Duration ttl) {
        String key = JwtCacheKey.generateBlacklistKey(identifier);
        redisTemplate.opsForValue().set(key, dataSerializer.serializeOrException(value), ttl);
    }

    @Override
    public JwtDetail getBlacklistToken(String identifier) {
        String key = JwtCacheKey.generateBlacklistKey(identifier);
        String jwtDetail = redisTemplate.opsForValue().get(key);
        return dataSerializer.deserializeOrNull(jwtDetail, JwtDetail.class);
    }

    @Override
    public boolean isBlacklisted(String identifier) {
        String key = JwtCacheKey.generateBlacklistKey(identifier);
        return redisTemplate.hasKey(key);
    }
}
