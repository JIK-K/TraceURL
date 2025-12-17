package com.traceurl.traceurl.core.shorturl.dto.request;

import com.traceurl.traceurl.common.base.BaseDto;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ShortUrlCreateRequestDto extends BaseDto {

    private String originalUrl;
    private String title;

}
