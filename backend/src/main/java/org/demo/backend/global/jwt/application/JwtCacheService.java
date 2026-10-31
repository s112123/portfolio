package org.demo.backend.global.jwt.application;

import lombok.RequiredArgsConstructor;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.jwt.application.dto.JwtDetail;
import org.demo.backend.global.jwt.domain.JwtCacheRepository;
import org.demo.backend.global.jwt.domain.JwtErrorCode;
import org.demo.backend.global.jwt.domain.JwtType;
import org.demo.backend.global.jwt.provider.JwtProvider;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtCacheService {

    private final JwtProvider jwtProvider;
    private final JwtCacheRepository<JwtDetail> jwtCacheRepository;

    // 토큰을 캐시 저장소에 저장
    public void saveTokenInCache(String token, JwtType type) {
        JwtDetail jwtDetail = toJwtDetail(token, type);
        Duration ttl = Duration.between(LocalDateTime.now(), jwtDetail.getExpiredAt());
        jwtCacheRepository.saveToken(token, jwtDetail, ttl);
    }

    // 토큰을 블랙리스트에만 저장
    public void saveTokenInBlacklist(String token, JwtType type) {
        // 1. Refresh Token 의 만료시간 남은 경우, 남은 시간만큼 블랙 리스트에 저장한다
        JwtDetail jwtDetail = toJwtDetail(token, type);
        Duration remainingTime = Duration.between(LocalDateTime.now(), jwtDetail.getExpiredAt());
        if (remainingTime.toMillis() > 0) {
            jwtCacheRepository.saveBlacklistToken(jwtDetail.getToken(), jwtDetail, remainingTime);
        }
        // 2. 캐시 저장소에 저장된 현재 Token 은 삭제
        invalidateTokenInCache(token);
    }

    // 토큰을 블랙리스트에 저장 → 유효성 검사 포함
    public void verifyAndSaveTokenInBlacklist(String token) {
        // 1. 캐시 저장소에서 실제 발급된 토큰인지 확인
        JwtDetail jwtDetail = verifyIssuedToken(token);

        // 2. 탈취 토큰인지 확인
        // 캐시 저장소에 없다면 블랙리스트에 있는지 확인
        if (jwtDetail == null) {
            // 블랙 리스트로 등록되었으면 사전에 탈취되었을 수 있다
            if (jwtCacheRepository.isBlacklisted(token)) {
                // TODO: 해당 유저의 모든 기기 강제 로그아웃 및 모든 토큰 폐기
                throw new BusinessException(JwtErrorCode.TOKEN_THEFT_DETECTED);
            }
            // 블랙 리스트에도 없으면 너무 오래되어 증발한 단순 기간 만료 토큰
            throw new BusinessException(JwtErrorCode.EXPIRED);
        }

        // 3. Refresh Token 의 만료시간 남은 경우, 남은 시간만큼 블랙 리스트에 저장한다
        // Duration.between(start, end) → end - start
        // (end - start) > 0 → 만료 기간이 남음
        // (end - start) < 0 → 만료 기간이 없음
        Duration remainingTime = Duration.between(LocalDateTime.now(), jwtDetail.getExpiredAt());
        if (remainingTime.toMillis() > 0) {
            jwtCacheRepository.saveBlacklistToken(jwtDetail.getToken(), jwtDetail, remainingTime);
        }

        // 4. 캐시 저장소에 저장된 현재 Token 은 삭제
        invalidateTokenInCache(token);
    }

    // 실제 발급된 토큰인지 확인
    public JwtDetail verifyIssuedToken(String token) {
        JwtDetail jwtDetail = jwtCacheRepository.getToken(token);
        if (jwtDetail == null) {
            throw new BusinessException(JwtErrorCode.INVALID);
        }
        return jwtDetail;
    }

    // 캐시 삭제
    public void invalidateTokenInCache(String token) {
        jwtCacheRepository.invalidateToken(token);
    }

    // 블랙리스트 토큰인지 여부
    public boolean isBlacklisted(String token) {
        return jwtCacheRepository.isBlacklisted(token);
    }

    // 문자열 토큰을 JwtDetail 객체로 변환
    private JwtDetail toJwtDetail(String token, JwtType type) {
        Date expiration = jwtProvider.getExpiration(token);
        LocalDateTime expiredAt = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return JwtDetail.builder()
                .memberId(Long.valueOf(jwtProvider.getSubject(token)))
                .type(type)
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiredAt(expiredAt)
                .build();
    }
}
