package com.traceurl.traceurl.core.analytics.controller;

import com.traceurl.traceurl.common.dto.ResponseDto;
import com.traceurl.traceurl.core.analytics.dto.request.IpBlocklistCreateRequestDto;
import com.traceurl.traceurl.core.analytics.dto.response.IpBlocklistResponseDto;
import com.traceurl.traceurl.core.analytics.entity.IpBlocklist;
import com.traceurl.traceurl.core.analytics.service.IpBlocklistService;
import com.traceurl.traceurl.core.shorturl.dto.response.ShortUrlResponseDto;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/block-list")
@RequiredArgsConstructor
public class IpBlocklistController {
    private final IpBlocklistService ipBlocklistService;

    @PostMapping("/create")
    public ResponseDto<IpBlocklistResponseDto> crate(
            @RequestBody IpBlocklistCreateRequestDto ipBlocklistCreateRequestDto,
            Authentication authentication
    ){
        UUID userId = (UUID) authentication.getPrincipal();
        ResponseDto<IpBlocklistResponseDto> response = new ResponseDto<>();
        response.setSuccess(ipBlocklistService.blockIp(ipBlocklistCreateRequestDto, userId));
        return response;
    }

    @GetMapping("/list/{shortCode}")
    public ResponseDto<List<IpBlocklistResponseDto>> list(
            @PathVariable String shortCode,
            Pageable pageable,
            Authentication authentication
    ){
        UUID userId = UUID.fromString(authentication.getName());

        List<IpBlocklistResponseDto> data = ipBlocklistService.getMyBlockedIps(
                userId,
                pageable,
                shortCode
        );

        ResponseDto<List<IpBlocklistResponseDto>> response = new ResponseDto<>();
        response.setSuccess(data);
        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseDto<Void> unblock(
            @PathVariable UUID id,
            Authentication authentication
    ){
        UUID userId = (UUID) authentication.getPrincipal();
        ipBlocklistService.unblockIp(id, userId);

        ResponseDto<Void> response = new ResponseDto<>();
        response.setSuccess(null); // 성공 응답
        return response;
    }
}
