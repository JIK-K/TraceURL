package com.traceurl.traceurl.core;

import com.traceurl.traceurl.common.util.ip.IpUtils;
import com.traceurl.traceurl.core.analytics.service.ClickEventService;
import com.traceurl.traceurl.core.analytics.service.IpBlocklistService;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlRepository;
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

    private final ShortUrlRepository shortUrlRepository;
    private final IpBlocklistService ipBlocklistService;
    private final ClickEventService clickEventService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(name = "v_id", required = false) String vId
    ) {
        // 1. 단축 URL 조회
        ShortUrl shortUrlEntity = shortUrlRepository.findActiveByShortCode(shortCode);
        if (shortUrlEntity == null) return ResponseEntity.notFound().build();

        // 2. 접속 정보 추출 (비동기 처리를 위해 컨트롤러에서 미리 추출)
        String clientIp = IpUtils.getClientIp(request);
        String uaRaw = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        // 3. 차단 여부 확인
        boolean isBlocked = ipBlocklistService.isBlocked(shortUrlEntity.getId(), clientIp);

        // 4. Visitor ID 처리 (쿠키)
        String visitorId = vId;
        boolean isNewVisitor = false;
        if (visitorId == null) {
            visitorId = UUID.randomUUID().toString();
            isNewVisitor = true;
            Cookie cookie = new Cookie("v_id", visitorId);
            cookie.setMaxAge(60 * 60 * 24 * 365); // 1년
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

        clientIp = "161.185.160.93";
        // 5. 비동기 로그 저장 (추출한 문자열 데이터들을 전달)
        clickEventService.logClick(
                shortUrlEntity.getId(),
                clientIp,
                uaRaw,
                referrer,
                visitorId,
                isNewVisitor,
                !isBlocked
        );

        // 6. 차단 시 응답
        if (isBlocked) {
            log.warn("Blocked access attempt: IP={}, Code={}", clientIp, shortCode);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 7. 정상 리다이렉트
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(shortUrlEntity.getOriginalUrl()))
                .build();
    }
}