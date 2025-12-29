package com.traceurl.traceurl.core.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AnalyticsChartResponseDto {
    private List<ChartPoint> points;

    @Getter @Setter @AllArgsConstructor
    public static class ChartPoint {
        private String label; // "14:00" 또는 "2023-10-21"
        private long pv;
        private long uv;
    }
}