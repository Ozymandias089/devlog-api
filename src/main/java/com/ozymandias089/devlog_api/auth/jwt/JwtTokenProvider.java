package com.ozymandias089.devlog_api.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// todo: Fucking figure out how jjwt 0.12.x works
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenValidityInMs;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidityInMs;

    private final StringRedisTemplate redisTemplate;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Access Token 생성
    public String createAccessToken(String memberId) {
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenValidityInMs)))
                .claims(Map.of("sub", memberId))
                .signWith(key, Jwts.SIG.HS256);

        return builder.compact();
    }

    // Refresh Token 생성 및 Redis 저장
    public String createRefreshToken(String memberId) {
        Instant now = Instant.now();
        String refreshToken = Jwts.builder()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenValidityInMs)))
                .claims(Map.of("sub", memberId))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        redisTemplate.opsForValue().set("RT:" + memberId, refreshToken, refreshTokenValidityInMs, TimeUnit.MILLISECONDS);
        return refreshToken;
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 회원 ID(subject) 추출
    public String getMemberIdFromToken(String token) {
        JwtParser parser = Jwts.parser().verifyWith(key).build();
        Jwt<?, ?> parsed = parser.parse(token);
        Claims claims = ((Jws<Claims>) parsed).getPayload();
        return claims.getSubject(); // 혹은 claims.get("sub", String.class)
    }

    // Redis에 저장된 Refresh Token 조회
    public String getStoredRefreshToken(String memberId) {
        return redisTemplate.opsForValue().get("RT:" + memberId);
    }

    // Redis에서 Refresh Token 삭제
    public void deleteRefreshToken(String memberId) {
        redisTemplate.delete("RT:" + memberId);
    }
}
