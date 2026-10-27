package org.demo.backend.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.auth.application.AuthService;
import org.demo.backend.auth.application.dto.TokenDetail;
import org.demo.backend.auth.controller.dto.LoginRequest;
import org.demo.backend.global.api.dto.ApiResponse;
import org.demo.backend.global.jwt.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        // 1. 로그인
        TokenDetail tokenDetail = authService.login(request);

        // 2. Refresh Token 은 쿠키로 전송 (보안 강화)
        // 학습이므로 secure 는 false 로 한다
        // secure 를 true 로 하는 경우, 쿠키 이름을 __Host- 접두사를 붙여 브라우저 표준 보안 쿠키 네이밍 규칙을 사용할 수 있다
        ResponseCookie refreshTokenCookie = ResponseCookie.from("itBlogRefreshToken", tokenDetail.getRefreshToken())
                .httpOnly(true)     // XSS 방지
                .secure(false)      // HTTPS 전송 → 학습이므로 false 를 하여 HTTP 전송도 가능하도록 한다
                .sameSite("Lax")    // CSRF 방지
                .path("/")
                .maxAge(jwtProvider.getRefreshTokenExpiration() / 1000)
                .build();

        // 3. Access Token 은 클라이언트의 메모리 또는 LocalStorage 에 저장하므로 응답한다
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(tokenDetail.getAccessToken()));
    }
}
