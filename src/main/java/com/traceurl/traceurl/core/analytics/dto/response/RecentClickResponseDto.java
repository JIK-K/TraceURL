package com.traceurl.traceurl.core.analytics.dto.response;

import com.traceurl.traceurl.common.base.BaseDto;
import com.traceurl.traceurl.core.analytics.entity.ClickEvent;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.time.*;

@Getter
@Setter
@SuperBuilder
public class RecentClickResponseDto extends BaseDto {
    private String time;         // "Just now", "2 mins ago" 등 (프론트에서 처리하거나 서버에서 계산)
    private String ip;           // 123.123.***.***
    private String location;     // "Seoul, KR"
    private String locationCode; // "kr"
    private String device;       // "Chrome (Windows)"
    private String deviceIcon;   // "desktop_windows", "smartphone" 등
    private String referrer;     // "google.com" (도메인만)
    private String referrerFull; // 전체 URL
    private String type;         // "New" 또는 "Returning"
    private String flagImage;    // "https://flagcdn.com/w40/kr.png"

    public static RecentClickResponseDto from(ClickEvent event){
        String countryCode = (event.getGeoCountryCode() != null) ? event.getGeoCountryCode().toLowerCase() : "unknown";

        return RecentClickResponseDto.builder()
                .time(formatTimeAgo(event.getCreatedAt()))
                .ip(event.getMaskedIp())
                .location(event.getGeoCity() + ", " + countryCode.toUpperCase())
                .locationCode(countryCode)
                .device(event.getUaBrowser() + " (" + event.getUaOs() + ")")
                .deviceIcon(mapDeviceIcon(event.getUaDeviceType()))
                .referrer(getDomainOnly(event.getReferrer()))
                .referrerFull(event.getReferrer())
                .type(event.getIsNewVisitor() ? "New" : "Returning")
                .flagImage("https://flagcdn.com/w40/" + countryCode + ".png")
                .createdAt(event.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(event.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .build();
    }

    private static String formatTimeAgo(Instant createdAt) {
        if (createdAt == null) return "Unknown";

        // Instant.now() 역시 UTC 기준이므로 Duration 계산이 정확함
        Duration duration = Duration.between(createdAt, Instant.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "Just now";
        if (seconds < 3600) return (seconds / 60) + " mins ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 2592000) return (seconds / 86400) + " days ago";

        // 한 달 이상은 날짜 표시 (시스템 기본 타임존 기준 날짜 반환)
        return createdAt.atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    /**
     * 디바이스 타입에 따른 머티리얼 아이콘 매핑
     */
    private static String mapDeviceIcon(String deviceType) {
        if (deviceType == null) return "desktop_windows";
        return switch (deviceType.toUpperCase()) {
            case "MOBILE" -> "smartphone";
            case "TABLET" -> "tablet_mac";
            default -> "desktop_windows";
        };
    }

    /**
     * 레퍼러 URL에서 도메인만 추출
     */
    private static String getDomainOnly(String url) {
        if (url == null || url.isEmpty() || url.equalsIgnoreCase("Direct")) return "Direct";
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return (host != null) ? host : url;
        } catch (Exception e) {
            return url; // 파싱 실패 시 원본 반환
        }
    }
}