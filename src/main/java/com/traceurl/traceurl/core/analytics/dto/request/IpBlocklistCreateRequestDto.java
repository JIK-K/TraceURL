package com.traceurl.traceurl.core.analytics.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class IpBlocklistCreateRequestDto {
    private String shortCode;
    private String ipAddress;
    private String reason;
}
