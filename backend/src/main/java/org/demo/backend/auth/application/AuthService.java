package org.demo.backend.auth.application;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.auth.application.dto.AuthDetail;
import org.demo.backend.auth.controller.dto.LoginRequest;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.jwt.application.JwtCacheService;
import org.demo.backend.global.jwt.domain.JwtType;
import org.demo.backend.global.jwt.persistence.JwtCacheKey;
import org.demo.backend.global.jwt.provider.JwtProvider;
import org.demo.backend.global.lock.LockService;
import org.demo.backend.global.security.CustomUserDetails;
import org.demo.backend.member.domain.Member;
import org.demo.backend.member.domain.MemberErrorCode;
import org.demo.backend.member.domain.MemberRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtCacheService jwtCacheService;
    private final MemberRepository memberRepository;
    private final LockService lockService;

    // 로그인
    public AuthDetail login(LoginRequest request) {
        // 1. 인증 전, authentication 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. AuthenticationManager 로 인증 처리 후, authentication 생성
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. JWT 발행
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // Access Token (유효시간: 10분)
        String accessToken = jwtProvider.generateAccessToken(userDetails);
        // Refresh Token (유효시간: 1일)
        String refreshToken = jwtProvider.generateRefreshToken(userDetails);

        // 4. Refresh Token 은 캐시 저장소 (Redis, Caffeine 등) 에 저장
        jwtCacheService.saveTokenInCache(refreshToken, JwtType.REFRESH);

        // 5. 인증된 객체를 SecurityContext 에 저장
        // 현재 로그인 요청에서 인증 정보를 사용할 일이 없다면 생략해도 상관없다
        // SecurityContextHolder.getContext().setAuthentication(authentication);

        return new AuthDetail(accessToken, refreshToken);
    }

    // JWT 갱신 (분산 락)
    public AuthDetail refreshWithLock(String refreshToken) {
        // 락 키 생성
        // 사용자마다 서로 다른 Refresh Token 을 락 키로 잡기 때문에 독립적으로 락이 적용된다
        String lockKey = JwtCacheKey.generateLockKey(refreshToken);
        return lockService.executeWithLock(lockKey, () -> refresh(refreshToken));
    }

    // JWT 갱신
    private AuthDetail refresh(String refreshToken) {
        // 1. Refresh Token 검증
        Claims claims = jwtProvider.validate(refreshToken);
        Long memberId = Long.valueOf(claims.getSubject());

        // 2. Refresh Token 이 블랙리스트에 저장하고 기존 캐시 저장소에서 삭제한다
        // 실제 발급한 토큰인지 확인 → 탈취 확인 → 블랙리스트 저장 → 캐시저장소에서 삭제
        jwtCacheService.verifyAndSaveTokenInBlacklist(refreshToken);

        // 3. JWT 재발행
        Member foundMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));
        CustomUserDetails userDetails = new CustomUserDetails(foundMember);
        // Access Token 재발행 (유효시간: 10분)
        String newAccessToken = jwtProvider.generateAccessToken(userDetails);
        // Refresh Token 재발행 (유효시간: 1일)
        String newRefreshToken = jwtProvider.generateRefreshToken(userDetails);

        // 4. 새로운 Refresh Token 은 캐시 저장소 (Redis, Caffeine 등) 에 저장
        jwtCacheService.saveTokenInCache(newRefreshToken, JwtType.REFRESH);
        return new AuthDetail(newAccessToken, newRefreshToken);
    }

    // 로그아웃
    public void logout(String accessToken, String refreshToken) {
        // 1. Access Token 을 블랙리스트에 넣음
        blacklistTokenIfPresent(
                accessToken,
                token -> jwtCacheService.saveTokenInBlacklist(token, JwtType.ACCESS)
        );

        // 2. Access Token 을 블랙리스트에 넣음
        blacklistTokenIfPresent(
                refreshToken,
                token ->  jwtCacheService.saveTokenInBlacklist(token, JwtType.REFRESH)
        );
    }

    // 로그아웃은 사용자 경험을 해치지 않기 위해 예외는 그냥 흘러보내고 로그아웃되도록 한다
    private void blacklistTokenIfPresent(String token, Consumer<String> blacklistAction) {
        if (StringUtils.hasText(token)) {
            try {
                blacklistAction.accept(token);
            } catch (Exception e) {
                log.warn("[LOGOUT] 블랙리스트에 JWT 등록 실패", e);
            }
        }
    }
}
