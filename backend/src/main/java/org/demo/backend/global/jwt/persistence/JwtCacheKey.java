package org.demo.backend.global.jwt.persistence;

public class JwtCacheKey {

    // JWT 를 저장하는 키
    private static final String JWT_PREFIX = "jwt:";

    // JWT 를 블랙리스트로 저장하는 키
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    // JWT 를 갱신할 때, 사용할 분산 락 키
    private static final String LOCK_PREFIX = "jwt:lock:refresh:";

    public static String generateJwtKey(String identifier) {
        return JWT_PREFIX + identifier;
    }

    public static String generateBlacklistKey(String identifier) {
        return BLACKLIST_PREFIX + identifier;
    }

    public static String generateLockKey(String identifier) {
        return LOCK_PREFIX + identifier;
    }
}
