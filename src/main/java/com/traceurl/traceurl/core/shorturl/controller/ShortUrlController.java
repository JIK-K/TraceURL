package com.traceurl.traceurl.core.shorturl.controller;

import com.traceurl.traceurl.common.dto.ResponseDto;
import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.core.shorturl.dto.request.ShortUrlCreateRequestDto;
import com.traceurl.traceurl.core.shorturl.dto.request.ShortUrlEditRequestDto;
import com.traceurl.traceurl.core.shorturl.dto.response.ShortUrlEditResponseDto;
import com.traceurl.traceurl.core.shorturl.dto.response.ShortUrlResponseDto;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.service.ShortUrlService;
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
@RequestMapping("/short-url")
@RequiredArgsConstructor
public class ShortUrlController {
    private final ShortUrlService shortUrlService;

    @PostMapping("/create")
    public ResponseDto<Boolean> create(
            @RequestBody ShortUrlCreateRequestDto shortUrlCreateRequestDto,
            Authentication authentication
    ){
        UUID userId = (UUID) authentication.getPrincipal();
        ResponseDto<Boolean> response = new ResponseDto<>();
        Boolean result = shortUrlService.createShortUrl(shortUrlCreateRequestDto, userId);
        response.setSuccess(result);
        return response;
    }

    @GetMapping("/list")
    public ResponseDto<List<ShortUrlResponseDto>> list(
            @RequestParam(required = false) BaseStatus status,
            Pageable pageable,
            Authentication authentication
    ){
        UUID userId = (UUID) authentication.getPrincipal();

        Page<ShortUrl> page = shortUrlService.getMyShortUrls(
                userId,
                pageable,
                status
        );
        List<ShortUrlResponseDto> data = page.getContent().stream().map(ShortUrlResponseDto::from).toList();

        ResponseDto<List<ShortUrlResponseDto>> response = new ResponseDto<>();
        response.setSuccess(data);
        return response;
    }

    @GetMapping("link/{shortCode}/edit")
    public ResponseDto<ShortUrlEditResponseDto> getEdit(
            @PathVariable String shortCode,
            Authentication authentication
    ){
        UUID userId = (UUID) authentication.getPrincipal();
        ResponseDto<ShortUrlEditResponseDto> response = new ResponseDto<>();

        ShortUrlEditResponseDto data = shortUrlService.getShortUrlEditData(shortCode, userId);
        response.setSuccess(data);
        return response;
    }

    @PatchMapping("link/{shortCode}/edit")
    public ResponseDto<Boolean> patchEdit(
            @PathVariable String shortCode,
            @RequestBody ShortUrlEditRequestDto shortUrlEditRequestDto,
            Authentication authentication
    ){
        UUID userId = (UUID) authentication.getPrincipal();
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setSuccess(shortUrlService.patchShortUrlEditData(shortCode,userId,shortUrlEditRequestDto));
        return response;
    }

    @DeleteMapping("link/{shortCode}/edit")
    public ResponseDto<Boolean> delete(
            @PathVariable String shortCode,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        ResponseDto<Boolean> response = new ResponseDto<>();
        response.setSuccess(shortUrlService.deleteShortUrl(shortCode, userId));
        return response;
    }
}
