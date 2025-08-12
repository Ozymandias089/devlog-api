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

    /**
     * 비밀번호 재설정 토큰을 생성하고 Redis에 저장한다.
     * @param uuid 사용자 고유 식별자
     * @return 생성된 JWT 비밀번호 재설정 토큰
     */
    public String generatePasswordResetToken(String uuid) {
        long passwordResetTokenExpirationMinutes = 30L;
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(TimeUnit.MINUTES.toSeconds(passwordResetTokenExpirationMinutes));

        String token = Jwts.builder()
                .subject(uuid)
                .claim("type", "password_reset")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(secretKey)
                .compact();

        log.info("Password Reset Token Created for uuid: {}", uuid);

        String redisKey = "PRT:" + uuid;
        try {
            stringRedisTemplate.opsForValue().set(redisKey, token, passwordResetTokenExpirationMinutes, TimeUnit.MINUTES);
            log.info("Password Reset Token stored in Redis for uuid: {}", uuid);
        } catch (Exception e) {
            log.error("Failed to store password reset token for uuid {}: {}", uuid, e.getMessage(), e);
            throw new JwtValidationException("Failed to store Password Reset Token", e);
        }

        return token;
    }

    /**
     * JWT 토큰에서 Claims를 파싱한다.
     * @param token JWT 토큰 문자열
     * @return 파싱된 Claims 객체
     * @throws JwtValidationException 유효하지 않은 토큰일 경우 발생
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new JwtValidationException("Invalid JWT token", e);
        }
    }

    /**
     * 비밀번호 재설정 토큰인지 유효성 검사한다.
     * @param token 검사할 JWT 토큰
     * @return 토큰이 유효하고 타입이 "password_reset"일 경우 true
     */
    public boolean isPasswordResetTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);

            String type = claims.get("type", String.class);
            if (!"password_reset".equals(type)) {
                return false;
            }

            return true;
        } catch (JwtValidationException e) {
            return false;
        }
    }

    /**
     * Redis에 저장된 비밀번호 재설정 토큰과 비교하여 일치하는지 확인한다.
     * @param uuid 사용자 고유 식별자
     * @param token 비교할 토큰
     * @return 저장된 토큰과 일치하면 true
     */
    public boolean isPasswordResetTokenStored(String uuid, String token) {
        String redisKey = "PRT:" + uuid;
        String storedToken = stringRedisTemplate.opsForValue().get(redisKey);
        return token.equals(storedToken);
    }

    /**
     * JWT 토큰에서 subject(사용자 식별자)를 추출한다.
     * @param token JWT 토큰 문자열
     * @return 토큰 내 subject 값
     * @throws JwtValidationException 유효하지 않은 토큰일 경우 발생
     */
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

    /**
     * JWT 토큰의 기본적인 유효성을 검사한다.
     * @param token 검사할 JWT 토큰
     * @return 토큰이 유효하면 true
     */
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

    /**
     * UUID에 저장된 Refresh Token과 요청받은 토큰이 일치하는지 검증한다.
     * @param uuid 사용자 고유 식별자
     * @param requestToken 요청받은 Refresh Token
     * @return 저장된 토큰과 일치하면 true
     */
    public boolean validateRefreshToken(String uuid, String requestToken) {
        String storedToken = getRefreshToken(uuid);
        return storedToken != null && storedToken.equals(requestToken);
    }

    /**
     * UUID에 해당하는 Refresh Token을 삭제한다.
     * @param uuid 사용자 고유 식별자
     */
    public void deleteRefreshToken(String uuid) {
        stringRedisTemplate.delete("RT:" + uuid);
    }

    /**
     * UUID에 저장된 Refresh Token을 조회한다.
     * @param uuid 사용자 고유 식별자
     * @return 저장된 Refresh Token 문자열 또는 null
     */
    public String getRefreshToken(String uuid) {
        return stringRedisTemplate.opsForValue().get("RT:" + uuid);
    }

    /**
     * 액세스 토큰을 블랙리스트에 등록하여 만료까지 유효하지 않도록 처리한다.
     * @param token 블랙리스트에 추가할 액세스 토큰
     * @param expirationMillis 토큰 만료까지 남은 시간(밀리초)
     */
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

    /**
     * 액세스 토큰이 블랙리스트에 존재하는지 확인한다.
     * @param token 확인할 액세스 토큰
     * @return 블랙리스트에 존재하면 true
     */
    public boolean isAccessTokenBlacklisted(String token) {
        return stringRedisTemplate.hasKey("BL:" + token);
    }

    /**
     * JWT 토큰에서 사용자 권한(role) 정보를 추출한다.
     * @param token JWT 토큰 문자열
     * @return 토큰 내 roles claim 값
     * @throws JwtValidationException 유효하지 않은 토큰일 경우 발생
     */
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
