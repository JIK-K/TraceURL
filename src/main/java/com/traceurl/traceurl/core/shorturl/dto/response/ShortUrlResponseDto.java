package com.traceurl.traceurl.core.shorturl.dto.response;

import com.traceurl.traceurl.common.base.BaseDto;
import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@SuperBuilder
public class ShortUrlResponseDto extends BaseDto {
    private UUID id;
    private String shortCode;
    private String originalUrl;
    private String title;
    private Boolean isCustom;
    private BaseStatus status;

    public static ShortUrlResponseDto from(ShortUrl entity) {
        return ShortUrlResponseDto.builder()
                .id(entity.getId())
                .shortCode(entity.getShortCode())
                .originalUrl(entity.getOriginalUrl())
                .title(entity.getTitle())
                .isCustom(entity.getIsCustom())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .build();
    }
}
