package com.traceurl.traceurl.core.shorturl.dto.request;

import com.traceurl.traceurl.common.base.BaseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class ShortUrlCreateRequestDto{

    private String originalUrl;
    private String title;
    private Boolean isCustom;
    private String alias;
    private String expireDate;

}
