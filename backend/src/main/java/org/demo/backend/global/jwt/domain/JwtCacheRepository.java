package org.demo.backend.global.jwt.domain;

import java.time.Duration;

public interface JwtCacheRepository<T> {

    // 토큰 저장
    void saveToken(String identifier, T value, Duration ttl);

    // 토큰 조회
    T getToken(String identifier);

    // 토큰 삭제
    void invalidateToken(String identifier);

    // 블랙리스트 토큰 저장
    void saveBlacklistToken(String identifier, T value, Duration ttl);

    // 블랙리스트 토큰 조회
    T getBlacklistToken(String identifier);

    // 블랙리스트 토큰인지 확인
    boolean isBlacklisted(String identifier);
}
