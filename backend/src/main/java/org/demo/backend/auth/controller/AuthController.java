package org.demo.backend.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.auth.application.AuthService;
import org.demo.backend.auth.application.dto.AuthDetail;
import org.demo.backend.auth.controller.dto.LoginRequest;
import org.demo.backend.global.api.dto.ApiResponse;
import org.demo.backend.global.exception.BusinessException;
import org.demo.backend.global.jwt.domain.JwtErrorCode;
import org.demo.backend.global.jwt.provider.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final String REFRESH_TOKEN_NAME = "itBlogRefreshToken";

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        // 1. 로그인
        AuthDetail authDetail = authService.login(request);
        // 2. Refresh Token 은 쿠키로 전송 (보안 강화)
        // 학습이므로 secure 는 false 로 한다
        // secure 를 true 로 하는 경우, 쿠키 이름을 __Host- 접두사를 붙여 브라우저 표준 보안 쿠키 네이밍 규칙을 사용할 수 있다
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, authDetail.getRefreshToken())
                .httpOnly(true)     // XSS 방지
                .secure(false)      // HTTPS 전송 → 학습이므로 false 를 하여 HTTP 전송도 가능하도록 한다
                .sameSite("Lax")    // CSRF 방지
                .path("/")
                .maxAge(jwtProvider.getRefreshTokenExpiration() / 1000)
                .build();
        // 3. Access Token 은 클라이언트의 메모리 또는 LocalStorage 에 저장하므로 응답한다
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(authDetail.getAccessToken()));
    }

    // JWT 갱신
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = REFRESH_TOKEN_NAME, required = false) String refreshToken
    ) {
        // 1. 쿠키에서 Refresh Token 추출
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(JwtErrorCode.NOT_FOUND);
        }
        // 2. JWT 갱신
        AuthDetail authDetail = authService.refreshWithLock(refreshToken);
        // 3. Refresh Token 은 쿠키로 전송 (보안 강화)
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, authDetail.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtProvider.getRefreshTokenExpiration() / 1000)
                .build();
        // 4. Access Token 은 클라이언트의 메모리 또는 LocalStorage 에 저장하므로 응답한다
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(authDetail.getAccessToken()));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String header,
            @CookieValue(name = REFRESH_TOKEN_NAME, required = false) String refreshToken
    ) {
        // 1. Access Token 추출
        String accessToken = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            accessToken = header.substring(7);
        }
        // 2. 로그아웃
        authService.logout(accessToken, refreshToken);
        // 3. Refresh Token 쿠키 제거 → 만료시간을 0으로 처리
        ResponseCookie expiredCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(ApiResponse.success("로그아웃되었습니다."));
    }
}
