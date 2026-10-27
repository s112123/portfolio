package org.demo.backend.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.security.CustomUserDetails;
import org.demo.backend.global.jwt.JwtProvider;
import org.demo.backend.auth.application.dto.TokenDetail;
import org.demo.backend.auth.controller.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    // 로그인
    public TokenDetail login(LoginRequest request) {
        // 1. 인증 전, authentication 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. AuthenticationManager 로 인증 처리 후, authentication 생성
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증된 객체를 SecurityContext 에 저장
        // 현재 로그인 요청에서 인증 정보를 사용할 일이 없다면 생략해도 상관없다
        // SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. JWT 발행
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // Access Token (유효시간: 10분)
        String accessToken = jwtProvider.generateAccessToken(userDetails);
        // Refresh Token (유효시간: 1일)
        String refreshToken = jwtProvider.generateRefreshToken(userDetails);

        // 5. Refresh Token 은 Redis 에 저장
        return new TokenDetail(accessToken, refreshToken);
    }
}
