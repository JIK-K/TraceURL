package com.traceurl.traceurl.core.analytics.service;

import com.traceurl.traceurl.common.util.ip.IpUtils;
import com.traceurl.traceurl.core.analytics.dto.common.GeoLocationDto;
import com.traceurl.traceurl.core.analytics.entity.ClickEvent;
import com.traceurl.traceurl.core.analytics.repository.ClickEventRepository;
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
    private final GeoLocationService geoLocationService;
    private final Parser uaParser = new Parser();

    /**
     * 비동기 클릭 로그 저장
     * HttpServletRequest 대신 필요한 데이터(문자열)만 파라미터로 받음으로써 쓰레드 안전성 확보
     */
    @Async
    @Transactional
    public void logClick(
            UUID shortUrlId,
            String clientIp,
            String uaRaw,
            String referrer,
            String visitorId,
            boolean isNewVisitor,
            boolean isValid
    ) {
        try {
            // 1. IP 기반 정보 생성
            String ipHash = IpUtils.hashIp(clientIp);
            String maskedIp = IpUtils.maskIp(clientIp);
            GeoLocationDto geo = geoLocationService.getLocation(clientIp);

            // 2. User-Agent 파싱
            Client client = uaParser.parse(uaRaw);

            // 3. 엔티티 생성 및 저장
            ClickEvent event = ClickEvent.builder()
                    .shortUrlId(shortUrlId)
                    .visitorId(visitorId)
                    .isNewVisitor(isNewVisitor)
                    .ipHash(ipHash)
                    .maskedIp(maskedIp)
                    .geoCountry(geo.getCountry())
                    .geoRegion(geo.getRegion())
                    .geoCity(geo.getCity())
                    .uaRaw(uaRaw)
                    .uaDeviceType(parseDeviceType(uaRaw))
                    .uaOs(client.os.family)
                    .uaBrowser(client.userAgent.family)
                    .referrer(referrer)
                    .isValid(isValid)
                    .build();

            clickEventRepository.save(event);

        } catch (Exception e) {
            log.error("Failed to log click event for shortUrlId {}: {}", shortUrlId, e.getMessage());
        }
    }

    private String parseDeviceType(String ua) {
        if (ua == null) return "UNKNOWN";
        String lowercaseUa = ua.toLowerCase();
        if (lowercaseUa.contains("mobile") || lowercaseUa.contains("android") || lowercaseUa.contains("iphone")) return "MOBILE";
        if (lowercaseUa.contains("tablet") || lowercaseUa.contains("ipad")) return "TABLET";
        if (lowercaseUa.contains("bot") || lowercaseUa.contains("crawler")) return "BOT";
        return "DESKTOP";
    }
}