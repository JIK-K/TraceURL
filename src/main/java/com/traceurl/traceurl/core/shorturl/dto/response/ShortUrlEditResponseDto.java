package com.traceurl.traceurl.core.shorturl.dto.response;

import com.traceurl.traceurl.common.base.BaseDto;
import com.traceurl.traceurl.common.enums.BaseStatus;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrl;
import com.traceurl.traceurl.core.shorturl.entity.ShortUrlLifecycle;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@SuperBuilder
public class ShortUrlEditResponseDto extends BaseDto {
    private UUID id;
    private String shortCode;
    private String originalUrl;
    private String title;
    private BaseStatus status;
    private OffsetDateTime expireAt;
    private Boolean autoDelete;

    public static ShortUrlEditResponseDto from(
            ShortUrl shortUrl,
            ShortUrlLifecycle lifecycle
    ) {
        return ShortUrlEditResponseDto.builder()
                .id(shortUrl.getId())
                .shortCode(shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .title(shortUrl.getTitle())
                .status(shortUrl.getStatus())
                .expireAt(
                        lifecycle.getExpireAt() != null
                                ? lifecycle.getExpireAt().atOffset(ZoneOffset.UTC)
                                : null
                )
                .autoDelete(lifecycle.getAutoDelete())
                .createdAt(shortUrl.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(shortUrl.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .build();
    }
}
