package com.traceurl.traceurl.core.analytics.controller;

import com.traceurl.traceurl.common.dto.ResponseDto;
import com.traceurl.traceurl.core.analytics.dto.response.AnalyticsSummaryResponseDto;
import com.traceurl.traceurl.core.analytics.dto.response.RecentClickResponseDto;
import com.traceurl.traceurl.core.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}/recent")
    public ResponseDto<List<RecentClickResponseDto>> getRecentClicks(
            @PathVariable String shortCode,
            Pageable pageable, // size, page, sort를 자동으로 처리
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        List<RecentClickResponseDto> data = analyticsService.getRecentClicks(
                userId,
                shortCode,
                pageable
        );

        ResponseDto<List<RecentClickResponseDto>> response = new ResponseDto<>();
        response.setSuccess(data);
        return response;
    }

    @GetMapping("/{shortCode}/summary")
    public ResponseDto<AnalyticsSummaryResponseDto> getSummaryClicks(
            @PathVariable String shortCode,
            Authentication authentication
    ){
        UUID userId = UUID.fromString(authentication.getName());
        AnalyticsSummaryResponseDto data = analyticsService.getSummary(
                userId,
                shortCode
        );
        ResponseDto<AnalyticsSummaryResponseDto> response = new ResponseDto<>();
        response.setSuccess(data);
        return response;
    }
}
