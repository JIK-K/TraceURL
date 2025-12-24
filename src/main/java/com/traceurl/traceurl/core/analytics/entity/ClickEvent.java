package com.traceurl.traceurl.core.analytics.entity;

import com.traceurl.traceurl.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 단축 URL 클릭 시 발생하는 모든 원시 이벤트를 기록하는 엔티티
 * 성능을 위해 ShortUrl 엔티티와 물리적 외래키(FK) 연결을 하지 않음
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "click_events", indexes = {
        @Index(name = "idx_click_events_short_url_id", columnList = "short_url_id"),
        @Index(name = "idx_click_events_created_at", columnList = "created_at")
})
public class ClickEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // 클릭 이벤트 고유 식별자 (UUID v4)

    @Column(name = "short_url_id", nullable = false, columnDefinition = "UUID")
    private UUID shortUrlId; // 클릭된 단축 URL의 ID (FK 없이 값만 저장)

    @Column(name = "visitor_id")
    private String visitorId; // 브라우저 쿠키 또는 로컬 스토리지 기반의 고유 방문자 식별값

    @Column(name = "is_new_visitor")
    private Boolean isNewVisitor; // 해당 단축 URL 기준 첫 방문 여부 (UV 계산 시 활용)

    @Column(name = "ip_hash", length = 64)
    private String ipHash; // 접속자 IP를 SHA-256 등으로 해싱한 값 (개인정보 보호 및 중복 체크용)

    // --- Geo Location (지리 정보) ---
    private String geoCountry; // 접속 국가 코드 (예: KR, US)
    private String geoRegion;  // 접속 지역/주 (예: Seoul, California)
    private String geoCity;    // 접속 도시 (예: Gangnam-gu, Mountain View)

    // --- User Agent (브라우저 및 기기 정보) ---
    @Column(columnDefinition = "TEXT")
    private String uaRaw; // 클라이언트가 보낸 User-Agent 전체 문자열 (원본 보존용)

    private String uaDeviceType; // 기기 유형 (MOBILE, DESKTOP, TABLET, BOT 등)
    private String uaOs;         // 운영체제 정보 (Windows, iOS, Android 등)
    private String uaBrowser;    // 브라우저 정보 (Chrome, Safari, Firefox 등)

    @Column(columnDefinition = "TEXT")
    private String referrer; // 이전 페이지 URL (어떤 경로를 통해 유입되었는지 기록)

    @Column(name = "masked_ip", length = 25)
    private String maskedIp; // 123.123.***.*** 형태로 저장될 컬럼

    @Column(name = "is_valid")
    private Boolean isValid; // 유효한 클릭 여부 (IP 차단 목록 포함 여부나 봇 필터링 결과)

}