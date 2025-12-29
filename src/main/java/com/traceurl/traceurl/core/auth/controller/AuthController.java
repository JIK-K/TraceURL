package com.traceurl.traceurl.core.auth.controller;

import com.traceurl.traceurl.common.constant.JwtError;
import com.traceurl.traceurl.common.dto.ResponseDto;
import com.traceurl.traceurl.common.dto.TokenDto;
import com.traceurl.traceurl.common.exception.BusinessException;
import com.traceurl.traceurl.core.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/refresh")
    public ResponseDto<TokenDto> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        ResponseDto<TokenDto> responseDto = new ResponseDto<>();

        try {
            // 1. tra_rtk 쿠키 찾기
            Cookie refreshCookie = Arrays.stream(request.getCookies())
                    .filter(c -> "tra_rtk".equals(c.getName()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(JwtError.REFRESH_TOKEN_INVALID));

            String refreshToken = refreshCookie.getValue();


            // 2. RefreshToken 검증 & 새로운 AccessToken 발급
            String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken);

            // 3. 새 AccessToken 쿠키 설정
            Cookie newAccessCookie = new Cookie("tra_atk", newAccessToken);
            newAccessCookie.setPath("/");
            newAccessCookie.setMaxAge(60 * 60); // 1시간
            response.addCookie(newAccessCookie);

            // 4. ResponseDto 반환
            responseDto.setSuccess(new TokenDto(newAccessToken, refreshToken));

        } catch (BusinessException e){
            responseDto.setFailed(JwtError.REFRESH_TOKEN_INVALID);
        }
        catch (Exception e) {
            // JWT 관련 예외나 쿠키 미존재 시
            responseDto.setFailed(JwtError.INVALID_TOKEN);
        }

        return responseDto;
    }
}
