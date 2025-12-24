package com.traceurl.traceurl.core;

import com.traceurl.traceurl.common.util.ip.IpUtils;
import com.traceurl.traceurl.core.analytics.service.ClickEventService;
import com.traceurl.traceurl.core.analytics.service.IpBlocklistService;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlRepository;
import com.traceurl.traceurl.core.shorturl.service.ShortUrlService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RootRedirectController {

    private final ShortUrlService shortUrlService;
    private final ShortUrlRepository shortUrlRepository;
    private final IpBlocklistService ipBlocklistService;
    private final ClickEventService clickEventService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request,
            HttpServletResponse response, // 쿠키 구우려면 필요
            @CookieValue(name = "v_id", required = false) String vId // 기존 쿠키 확인
    ) {
        ShortUrl shortUrlEntity = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlEntity == null) return ResponseEntity.notFound().build();

        String clientIp = IpUtils.getClientIp(request);
        boolean isBlocked = ipBlocklistService.isBlocked(shortUrlEntity.getId(), clientIp);

        // 1. Visitor ID 처리 (쿠키가 없으면 생성)
        String visitorId = vId;
        boolean isNewVisitor = false;
        if (visitorId == null) {
            visitorId = UUID.randomUUID().toString();
            isNewVisitor = true;
            Cookie cookie = new Cookie("v_id", visitorId);
            cookie.setMaxAge(60 * 60 * 24 * 365); // 1년 유지
            cookie.setPath("/");
            cookie.setHttpOnly(true); // 보안
            response.addCookie(cookie);
        }

        // 2. 비동기 로그 저장 (차단되었어도 로그는 남기되 isValid를 false로 전달)
        clickEventService.logClick(
                shortUrlEntity.getId(),
                request,
                visitorId,
                isNewVisitor,
                !isBlocked // 차단 안됐으면 true, 됐으면 false
        );

        if (isBlocked) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(shortUrlEntity.getOriginalUrl()))
                .build();
    }
}
