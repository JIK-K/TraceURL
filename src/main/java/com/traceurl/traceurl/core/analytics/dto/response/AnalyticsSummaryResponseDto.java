package com.traceurl.traceurl.core.analytics.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsSummaryResponseDto {
    private StatDetail pv;
    private StatDetail uv;
    private double newRate;      // 신규 방문자 비율
    private double returnRate;   // 재방문자 비율
    private TopCountry topCountry;

    @Getter @Setter @AllArgsConstructor
    public static class StatDetail {
        private long value;
        private double change; // 증감률 (%)
    }

    @Getter @Setter @Builder
    public static class TopCountry {
        private String name;
        private double percent;
        private String flagImage;
    }
}