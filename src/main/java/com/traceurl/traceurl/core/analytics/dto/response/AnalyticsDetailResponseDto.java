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
    private List<CountryItem> countries;
    private List<DetailItem> referrers;

    // 최종 조립을 위한 from 메서드
    public static AnalyticsDetailResponseDto from(
            List<DetailItem> devices,
            List<DetailItem> browsers,
            List<DetailItem> platforms,
            List<CountryItem> countries,
            List<DetailItem> referrers
    ) {
        return AnalyticsDetailResponseDto.builder()
                .devices(devices)
                .browsers(browsers)
                .platforms(platforms)
                .countries(countries)
                .referrers(referrers)
                .build();
    }

    @Getter
    @SuperBuilder
    public static class DetailItem {
        private String label;
        private long count;
        private double percentage;

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

    @Getter
    @SuperBuilder
    public static class CountryItem {
        private String countryCode;
        private String countryName;
        private String flagUrl;
        private long count;
        private double percentage; // 국가도 비중이 있으면 좋으니 추가

        // 국가 전용 from 메서드
        public static CountryItem from(Map<String, Object> row, long totalCount) {
            String code = (String) row.get("label"); // DB 저장된 ISO 코드 (예: KR)
            long count = (long) row.get("count");
            double percentage = totalCount == 0 ? 0 : Math.round((double) count / totalCount * 1000) / 10.0;

            // Locale을 이용해 국가 코드를 국가명으로 변환 (예: KR -> South Korea)
            String name = new java.util.Locale("", code).getDisplayCountry(java.util.Locale.ENGLISH);

            return CountryItem.builder()
                    .countryCode(code)
                    .countryName(name)
                    .flagUrl("https://flagcdn.com/w40/" + code.toLowerCase() + ".png")
                    .count(count)
                    .percentage(percentage)
                    .build();
        }
    }
}