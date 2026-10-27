package org.demo.backend.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.demo.backend.global.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Component
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;
    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;
    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    // Signature Key
    private SecretKey getSignatureKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // AccessToken 만들기
    public String generateAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // userDetails.getAuthorities() 로 저장하면 페이로드에 { "roles": [{ "authority": "ROLE_USER" }] } 로 저장된다
        // 아래처럼 하면 페이로드에 { "roles: ["ROLE_USER"] } 로 저장되어 파싱이 더 쉽다
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        claims.put("roles", roles);
        return generate(String.valueOf(userDetails.getId()), claims, accessTokenExpiration);
    }

    // RefreshToken 만들기
    public String generateRefreshToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        claims.put("roles", roles);
        return generate(String.valueOf(userDetails.getId()), claims, refreshTokenExpiration);
    }

    // JWT 만들기
    private String generate(String subject, Map<String, Object> claims, long expiration) {
        Date currentTime = new Date(System.currentTimeMillis());
        Date expirationTime = new Date(currentTime.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(currentTime)
                .expiration(expirationTime)
                .signWith(getSignatureKey())
                .compact();
    }
}
