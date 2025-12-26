package com.traceurl.traceurl.core.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Getter
@SuperBuilder
public class AnalyticsDetailResponseDto {
    private List<DetailItem> devices;
    private List<DetailItem> browsers;
    private List<DetailItem> platforms;

    // 조립을 위한 from 메서드
    public static AnalyticsDetailResponseDto from(
            List<DetailItem> devices,
            List<DetailItem> browsers,
            List<DetailItem> platforms
    ) {
        return AnalyticsDetailResponseDto.builder()
                .devices(devices)
                .browsers(browsers)
                .platforms(platforms)
                .build();
    }

    @Getter
    @SuperBuilder
    public static class DetailItem {
        private String label;
        private long count;
        private double percentage;

        // 집계 결과(Map)를 DTO로 변환하는 from 메서드
        public static DetailItem from(Map<String, Object> row, long totalCount) {
            long count = (long) row.get("count");
            double percentage = totalCount == 0 ? 0 : Math.round((double) count / totalCount * 1000) / 10.0;

            return DetailItem.builder()
                    .label((String) row.get("label"))
                    .count(count)
                    .percentage(percentage)
                    .build();
        }
    }
}
