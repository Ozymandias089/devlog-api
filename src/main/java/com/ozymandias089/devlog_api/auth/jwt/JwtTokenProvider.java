package com.ozymandias089.devlog_api.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.secret}")
    private final String secretKeyBase64;

    private SecretKey secretKey;

    @Value("${jwt.access-token-expiration}")
    private final long accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration}")
    private final long refreshTokenExpirationDays;

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
    public String generateAccessToken(String uuid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TimeUnit.MINUTES.toMillis(accessTokenExpirationMinutes));
        return Jwts.builder()
                .subject(uuid)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
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

        String redisKey = "RT:"+uuid;
        stringRedisTemplate.opsForValue().set(redisKey, refreshToken, refreshTokenExpirationDays, TimeUnit.DAYS);

        return refreshToken;
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
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
        // 블랙리스트 키: "BL:{token}"
        stringRedisTemplate.opsForValue().set("BL:" + token, "logout", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isAccessTokenBlacklisted(String token) {
        return stringRedisTemplate.hasKey("BL:" + token);
    }
}
