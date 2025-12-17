package com.traceurl.traceurl.core.auth.jwt;

import com.traceurl.traceurl.common.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.access-secret}")
    private String accessSecretKey;

    @Value("${jwt.refresh-secret}")
    private String refreshSecretKey;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60;        // 1시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; // 7일

    /* =========================
       Token 생성
     ========================= */

    public TokenDto createToken(String userId) {
        Date now = new Date();

        String accessToken = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .claim("type", "ACCESS")
                .signWith(SignatureAlgorithm.HS256, accessSecretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME))
                .claim("type", "REFRESH")
                .signWith(SignatureAlgorithm.HS256, refreshSecretKey)
                .compact();

        log.info("JWT created - userId={}", userId);

        return new TokenDto(accessToken, refreshToken);
    }

    /* =========================
       Access Token 검증
     ========================= */

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(accessSecretKey)
                    .parseClaimsJws(token)
                    .getBody();

            boolean valid = "ACCESS".equals(claims.get("type"));
            log.info("AccessToken validation result={}", valid);
            return valid;

        } catch (JwtException e) {
            log.warn("AccessToken validation failed: {}", e.getMessage());
            return false;
        }
    }

    /* =========================
       Refresh Token 검증
     ========================= */

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(refreshSecretKey)
                    .parseClaimsJws(token)
                    .getBody();

            boolean valid = "REFRESH".equals(claims.get("type"));
            log.info("RefreshToken validation result={}", valid);
            return valid;

        } catch (JwtException e) {
            log.warn("RefreshToken validation failed: {}", e.getMessage());
            return false;
        }
    }

    /* =========================
       Access Token 재발급
     ========================= */

    public String reissueAccessToken(String refreshToken) {
        log.info("Start reissuing access token");

        if (!validateRefreshToken(refreshToken)) {
            log.warn("Refresh token invalid");
            throw new IllegalArgumentException("Invalid refresh token");
        }

        UUID userId = getUserIdFromRefreshToken(refreshToken);
        log.info("Reissuing access token for userId={}", userId);

        Date now = new Date();

        String newAccessToken = Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .claim("type", "ACCESS")
                .signWith(SignatureAlgorithm.HS256, accessSecretKey)
                .compact();

        log.info("Access token reissued successfully");

        return newAccessToken;
    }

    /* =========================
       UserId 추출 (Access)
     ========================= */

    public UUID getUserIdFromAccessToken(String accessToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(accessSecretKey)
                .parseClaimsJws(accessToken)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    /* =========================
       UserId 추출 (Refresh)
     ========================= */

    public UUID getUserIdFromRefreshToken(String refreshToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(refreshSecretKey)
                .parseClaimsJws(refreshToken)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }
}
