package com.ozymandias089.devlog_api.auth.jwt;

import com.ozymandias089.devlog_api.global.enums.Role;
import com.ozymandias089.devlog_api.global.exception.JwtValidationException;
import com.ozymandias089.devlog_api.global.util.Functions;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.secret}")
    private String secretKeyBase64;

    private SecretKey secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationDays;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyBase64);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a new JWT access Token for the given uid.
     * @param uuid The uuid to be included in the token's subject.
     * @return A signed JWT access token string.
     */
    public String generateAccessToken(String uuid, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TimeUnit.MINUTES.toMillis(accessTokenExpirationMinutes));
        String accessToken = Jwts.builder()
                .subject(uuid)
                .claim("roles", Functions.roleToString(role))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
        log.info("Access Token Created for uuid: {}", uuid);
        return accessToken;
    }

    /**
     * Generates a new JWT refresh token for the given uuid
     * @param uuid The uuid to be included in the token's subject.
     * @return A Signed JWT refresh Token string
     */
    public String generateRefreshToken(String uuid) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(TimeUnit.DAYS.toSeconds(refreshTokenExpirationDays));

        String refreshToken = Jwts.builder()
                .subject(uuid)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(secretKey)
                .compact();
        log.info("Refresh Token Created for uuid: {}", uuid);

        String redisKey = "RT:"+uuid;
        try {
            stringRedisTemplate.opsForValue().set(redisKey, refreshToken, refreshTokenExpirationDays, TimeUnit.DAYS);
            log.info("Refresh Token stored in Redis for uuid: {}", uuid);
        } catch (Exception e) {
            log.info("Failed to store refresh token for uuid {}: {}", uuid, e.getMessage(), e);
            throw new JwtValidationException("Failed to store Refresh Token ", e);
        }
        return refreshToken;
    }

    public String getSubject(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch(JwtException | IllegalArgumentException e) {
            log.error("Failed to extract Subject From Token: {}", token);
            throw new JwtValidationException("Invalid JWT Token", e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT Token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String uuid, String requestToken) {
        String storedToken = getRefreshToken(uuid);
        return storedToken != null && storedToken.equals(requestToken);
    }

    public void deleteRefreshToken(String uuid) {
        stringRedisTemplate.delete("RT:" + uuid);
    }

    public String getRefreshToken(String uuid) {
        return stringRedisTemplate.opsForValue().get("RT:" + uuid);
    }

    public void blacklistAccessToken(String token, long expirationMillis) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long expiration = claims.getExpiration().getTime();
        long now = System.currentTimeMillis();
        long ttl = expiration - now;

        if (ttl > 0) {
            stringRedisTemplate.opsForValue().set("BL:" + token, "logout", ttl, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isAccessTokenBlacklisted(String token) {
        return stringRedisTemplate.hasKey("BL:" + token);
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("roles", String.class); // 토큰 발급 시 넣은 claim 이름과 일치해야 함
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract role from token: {}", token);
            throw new JwtValidationException("Invalid JWT Token", e);
        }
    }
}
