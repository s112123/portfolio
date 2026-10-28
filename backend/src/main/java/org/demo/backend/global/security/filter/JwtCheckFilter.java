package org.demo.backend.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.backend.global.exception.dto.ErrorDetail;
import org.demo.backend.global.jwt.application.JwtCacheService;
import org.demo.backend.global.jwt.domain.BlacklistedTokenException;
import org.demo.backend.global.jwt.provider.JwtProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtCheckFilter extends OncePerRequestFilter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final JwtProvider jwtProvider;
    private final JwtCacheService jwtCacheService;
    private final ObjectMapper objectMapper;

    // JWT 를 검증하지 않을 경로
    private final List<String> whiteList = Arrays.asList(
            "/api/v1/health",
            "/api/v1/otp/send/email",
            "/api/v1/otp/verify/email",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // log.info("requestURI={}", request.getRequestURI());
        // Authorization 헤더 추출
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // JWT 추출
            String token = authorizationHeader.substring(7);
            try {
                // JWT 검증
                Claims claims = jwtProvider.validate(token);
                // 블랙리스트에 등록된 토큰인지 검증
                if (jwtCacheService.isBlacklisted(token)) {
                    handleJwtException(response, new BlacklistedTokenException("이미 로그아웃되었거나 차단된 토큰입니다"));
                    return;
                }
                // JWT 페이로드 추출
                Long memberId = Long.valueOf(claims.getSubject());
                List<String> roles = claims.get("roles", List.class);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(memberId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException e) {
                handleJwtException(response, e);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    // 토큰 검증 시, 예외 처리
    private void handleJwtException(HttpServletResponse response, Exception e) throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDetail<String> errorDetail = new ErrorDetail<>(status, "INVALID_TOKEN", "토큰이 유효하지 않습니다");

        if (e instanceof ExpiredJwtException) {
            errorDetail = new ErrorDetail<>(status, "EXPIRED_TOKEN", "만료된 토큰입니다");
        } else if (e instanceof MalformedJwtException) {
            errorDetail = new ErrorDetail<>(status, "MALFORMED_TOKEN", "손상되거나 잘못된 형식의 토큰입니다");
        } else if (e instanceof SignatureException) {
            errorDetail = new ErrorDetail<>(status, "INVALID_SIGNATURE", "토큰 서명이 위변조되었거나 일치하지 않습니다");
        } else if (e instanceof UnsupportedJwtException) {
            errorDetail = new ErrorDetail<>(status, "UNSUPPORTED_TOKEN", "지원하지 않는 형식의 토큰입니다");
        } else if (e instanceof IllegalArgumentException) {
            errorDetail = new ErrorDetail<>(status, "ILLEGAL_ARGUMENT_TOKEN", "토큰 값이 비어있거나 잘못되었습니다");
        } else if (e instanceof BlacklistedTokenException) {
            errorDetail = new ErrorDetail<>(status, "BLACKLISTED_TOKEN", "이미 로그아웃되었거나 차단된 토큰입니다");
        }

        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorDetail));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        // "/api/v1/members" 경로는 POST 만 공개 허용하고 나머지는 JWT 를 검증한다
        if (requestURI.equals("/api/v1/members")) {
            return requestMethod.equalsIgnoreCase("POST");
        }
        return whiteList.stream().anyMatch(url -> antPathMatcher.match(url, requestURI));
    }
}
