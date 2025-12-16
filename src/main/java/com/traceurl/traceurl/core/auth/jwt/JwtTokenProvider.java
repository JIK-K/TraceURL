package com.traceurl.traceurl.core.auth.jwt;

import com.traceurl.traceurl.common.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.JwtException;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.access-secret}")
    private String accessSecretKey;

    @Value("${jwt.refresh-secret}")
    private String refreshSecretKey;

    private final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60;      // 1시간
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; // 7일

    public TokenDto createToken(String userId) {

        Date now = new Date();

        String accessToken = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, accessSecretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, refreshSecretKey)
                .compact();

        return new TokenDto(accessToken, refreshToken);
    }

    public boolean validateToken(String token) {
        // 검증 가능한 secret 목록 (user/admin 등 여러 경우 추가 가능)
        String[] secrets = { refreshSecretKey, accessSecretKey };

        for (String secret : secrets) {
            try {
                Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(token);
                return true; // 성공하면 true 반환
            } catch (JwtException e) {
                // 실패하면 다음 secret으로 시도
                continue;
            }
        }

        // 어떤 secret으로도 검증되지 않으면 false
        return false;
    }


    public String reissueAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        UUID userId = getUserId(refreshToken);
        Date now = new Date();

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, accessSecretKey)
                .compact();
    }


    public UUID getUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(accessSecretKey)
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

}
