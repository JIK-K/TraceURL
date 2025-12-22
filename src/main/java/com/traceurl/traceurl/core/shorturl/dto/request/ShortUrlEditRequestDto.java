package com.traceurl.traceurl.core.shorturl.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@Data
public class ShortUrlEditRequestDto {
    private String title;
    private String expireDate;
    private Boolean autoDelete;
}
