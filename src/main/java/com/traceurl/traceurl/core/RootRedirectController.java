package com.traceurl.traceurl.core;

import com.traceurl.traceurl.common.util.ip.IpUtils;
import com.traceurl.traceurl.core.analytics.service.IpBlocklistService;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlRepository;
import com.traceurl.traceurl.core.shorturl.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RootRedirectController {

    private final ShortUrlService shortUrlService;
    private final ShortUrlRepository shortUrlRepository;
    private final IpBlocklistService ipBlocklistService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            jakarta.servlet.http.HttpServletRequest request // Request 객체 추가
    ) {

        log.info("hello motherfucker");
        // 1. 단축 URL 정보 조회
        ShortUrl shortUrlEntity = shortUrlRepository.findByShortCode(shortCode);

        if (shortUrlEntity == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. 실제 접속자 IP 추출
        String clientIp = IpUtils.getClientIp(request);
        boolean isBlocked = ipBlocklistService.isBlocked(shortUrlEntity.getId(), clientIp);

        // 3. IP 차단 여부 확인
        if (isBlocked) {
            log.warn("접속 차단됨 - IP: {}, Code: {}", clientIp, shortCode);
            // 차단된 경우 403 Forbidden을 주거나, 특정 에러 페이지로 보냅니다.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("==== 리다이렉트 체크 ====");
        log.info("접속 시도 IP: {}", clientIp);
        log.info("단축 URL ID: {}", shortUrlEntity.getId());
        log.info("차단 여부 결과: {}", isBlocked);
        log.info("========================");

        // 4. 정상 리다이렉션
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(shortUrlEntity.getOriginalUrl()))
                .build();
    }
}
