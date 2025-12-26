package com.traceurl.traceurl.core.analytics.service;

import com.traceurl.traceurl.common.constant.CommonError;
import com.traceurl.traceurl.common.exception.BusinessException;
import com.traceurl.traceurl.core.analytics.dto.response.AnalyticsChartResponseDto;
import com.traceurl.traceurl.core.analytics.dto.response.AnalyticsDetailResponseDto;
import com.traceurl.traceurl.core.analytics.dto.response.AnalyticsSummaryResponseDto;
import com.traceurl.traceurl.core.analytics.dto.response.RecentClickResponseDto;
import com.traceurl.traceurl.core.analytics.entity.ClickEvent;
import com.traceurl.traceurl.core.analytics.entity.ClickStatsBreakdown;
import com.traceurl.traceurl.core.analytics.entity.ClickStatsDaily;
import com.traceurl.traceurl.core.analytics.repository.ClickEventRepository;
import com.traceurl.traceurl.core.analytics.repository.ClickStatsBreakdownRepository;
import com.traceurl.traceurl.core.analytics.repository.ClickStatsDailyRepository;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final ClickStatsDailyRepository clickStatsDailyRepository;
    private final ClickStatsBreakdownRepository clickStatsBreakdownRepository;
    private final ShortUrlRepository shortUrlRepository;

    /**
     * 특정 단축 URL의 최근 클릭 로그 리스트 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public List<RecentClickResponseDto> getRecentClicks(UUID userId, String shortCode, Pageable pageable) {
        // 1. 해당 shortCode가 현재 로그인한 유저의 소유인지 검증
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        // 2. DB에서 페이징 조회 (Sort는 Controller에서 넘어온 pageable에 포함됨)
        Page<ClickEvent> events = clickEventRepository.findByShortUrlId(shortUrl.getId(), pageable);

        // 3. DTO 변환 후 리스트로 반환 (.getContent() 사용)
        return events.getContent().stream()
                .map(event -> RecentClickResponseDto.from(event))
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponseDto getSummary(UUID userId, String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 1. 최근 2일치 통계 조회
        List<ClickStatsDaily> stats = clickStatsDailyRepository
                .findByShortUrlIdAndStatDateGreaterThanEqualOrderByStatDateDesc(shortUrl.getId(), yesterday);

        // NPE 방지를 위해 기본 데이터 객체 생성 (PV/UV 0으로 초기화)
        ClickStatsDaily emptyStat = ClickStatsDaily.builder().pv(0L).uv(0L).build();

        ClickStatsDaily todayStat = stats.stream()
                .filter(s -> s.getStatDate().equals(today))
                .findFirst().orElse(emptyStat);

        ClickStatsDaily yesterdayStat = stats.stream()
                .filter(s -> s.getStatDate().equals(yesterday))
                .findFirst().orElse(emptyStat);

        // 2. 증감률 계산
        double pvChange = calculateChange(todayStat.getPv(), yesterdayStat.getPv());
        double uvChange = calculateChange(todayStat.getUv(), yesterdayStat.getUv());

        // 3. 신규/재방문 비율 (PV 대비 UV 비중으로 계산)
        long currentPv = todayStat.getPv();
        double newRate = (currentPv > 0) ? (double) todayStat.getUv() / currentPv * 100 : 0;

        return AnalyticsSummaryResponseDto.builder()
                .pv(new AnalyticsSummaryResponseDto.StatDetail(todayStat.getPv(), pvChange))
                .uv(new AnalyticsSummaryResponseDto.StatDetail(todayStat.getUv(), uvChange))
                .newRate(Math.round(newRate * 10) / 10.0)
                .returnRate(Math.round((100 - newRate) * 10) / 10.0)
                .topCountry(getTopCountry(shortUrl.getId(), today))
                .build();
    }

    @Transactional(readOnly = true)
    public AnalyticsChartResponseDto getChartData(UUID userId, String shortCode, String range) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        // 1. 날짜 범위 설정 (7d, 30d 등)
        int days = range.equals("30d") ? 30 : 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 2. DB에서 데이터 조회
        List<ClickStatsDaily> stats = clickStatsDailyRepository
                .findByShortUrlIdAndStatDateBetweenOrderByStatDateAsc(shortUrl.getId(), startDate, endDate);

        // 3. 조회를 빠르게 하기 위해 Map으로 변환
        Map<LocalDate, ClickStatsDaily> statsMap = stats.stream()
                .collect(Collectors.toMap(ClickStatsDaily::getStatDate, s -> s));

        // 4. 시작일부터 종료일까지 루프를 돌며 데이터가 없으면 0으로 채움
        List<AnalyticsChartResponseDto.ChartPoint> points = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            ClickStatsDaily s = statsMap.get(date);
            points.add(new AnalyticsChartResponseDto.ChartPoint(
                    date.toString(),
                    s != null ? s.getPv() : 0L,
                    s != null ? s.getUv() : 0L
            ));
        }

        return new AnalyticsChartResponseDto(points);
    }

    public AnalyticsDetailResponseDto getBreakdownDetails(UUID userId, String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndOwnerUserId(shortCode, userId)
                .orElseThrow(() -> new BusinessException(CommonError.ENTITY_NOT_FOUND));

        return AnalyticsDetailResponseDto.from(
                convert(shortUrl.getId(), "DEVICE"),
                convert(shortUrl.getId(), "BROWSER"),
                convert(shortUrl.getId(), "OS")
        );
    }

    private List<AnalyticsDetailResponseDto.DetailItem> convert(UUID urlId, String dimension) {
        List<Map<String, Object>> results = clickStatsBreakdownRepository.getTotalBreakdownStats(urlId, dimension);
        long totalCount = results.stream().mapToLong(r -> (long) r.get("count")).sum();

        return results.stream()
                .map(row -> AnalyticsDetailResponseDto.DetailItem.from(row, totalCount))
                .collect(Collectors.toList());
    }

    private double calculateChange(long current, long previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return Math.round(((double) (current - previous) / previous * 100) * 10) / 10.0;
    }

    private AnalyticsSummaryResponseDto.TopCountry getTopCountry(UUID shortUrlId, LocalDate date) {
        List<ClickStatsBreakdown> topList = clickStatsBreakdownRepository.findTopCountry(shortUrlId, date, PageRequest.of(0, 1));

        if (topList.isEmpty()) {
            return AnalyticsSummaryResponseDto.TopCountry.builder().name("N/A").percent(0).flagImage("").build();
        }

        ClickStatsBreakdown top = topList.get(0);
        String countryCode = top.getDimensionValue().toLowerCase(); // "kr"

        // Java 표준 Locale을 사용하여 국가 코드를 읽기 좋은 이름으로 변환
        Locale locale = new Locale("", countryCode);
        String displayName = locale.getDisplayCountry(Locale.ENGLISH); // "South Korea"

        return AnalyticsSummaryResponseDto.TopCountry.builder()
                .name(displayName) // 화면에는 "South Korea" 전달
                .percent(100.0)
                .flagImage("https://flagcdn.com/w40/" + countryCode + ".png")
                .build();
    }
}