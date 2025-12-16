package com.traceurl.traceurl.core.auth.handler;

import com.traceurl.traceurl.common.dto.TokenDto;
import com.traceurl.traceurl.core.auth.jwt.JwtTokenProvider;
import com.traceurl.traceurl.core.auth.oauth.CustomOAuthUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler
        implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuthUser principal = (CustomOAuthUser) authentication.getPrincipal();
        UUID userId = principal.getUserId();

        TokenDto token = jwtTokenProvider.createToken(userId.toString());

        Cookie accessToken = new Cookie("tra_atk", token.getAccessToken());
        accessToken.setPath("/");
        accessToken.setMaxAge(60 * 60);

        Cookie refreshToken = new Cookie("tra_rtk", token.getRefreshToken());
        refreshToken.setPath("/");
        refreshToken.setMaxAge(60 * 60 * 24 * 7);

        response.addCookie(accessToken);
        response.addCookie(refreshToken);

        response.sendRedirect("http://localhost:3000");
    }
}
