package com.traceurl.traceurl.core.analytics.service;

import com.traceurl.traceurl.common.util.crypto.AesUtil;
import com.traceurl.traceurl.common.util.ip.IpUtils;
import com.traceurl.traceurl.core.analytics.entity.ClickEvent;
import com.traceurl.traceurl.core.analytics.repository.ClickEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickEventService {

    private final ClickEventRepository clickEventRepository;
    private final Parser uaParser = new Parser();

    @Async
    @Transactional
    public void logClick(UUID shortUrlId, HttpServletRequest request, String visitorId, boolean isNewVisitor, boolean isValid) {
        try {
            String clientIp = IpUtils.getClientIp(request);
            String ipHash = IpUtils.hashIp(clientIp); // 해싱 적용
            String maskedIp = IpUtils.maskIp(clientIp);
            String uaRaw = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");

            Client client = uaParser.parse(uaRaw);

            ClickEvent event = ClickEvent.builder()
                    .shortUrlId(shortUrlId)
                    .visitorId(visitorId)
                    .isNewVisitor(isNewVisitor)
                    .ipHash(ipHash)
                    .uaRaw(uaRaw)
                    .uaDeviceType(parseDeviceType(uaRaw))
                    .uaOs(client.os.family)
                    .uaBrowser(client.userAgent.family)
                    .referrer(referrer)
                    .maskedIp(maskedIp)
                    .isValid(isValid)
                    .build();

            clickEventRepository.save(event);
        } catch (Exception e) {
            log.error("로그 저장 중 에러 발생: {}", e.getMessage());
        }
    }

    private String parseDeviceType(String ua) {
        if (ua == null) return "UNKNOWN";
        String lowercaseUa = ua.toLowerCase();
        if (lowercaseUa.contains("mobile")) return "MOBILE";
        if (lowercaseUa.contains("tablet")) return "TABLET";
        return "DESKTOP";
    }
}
