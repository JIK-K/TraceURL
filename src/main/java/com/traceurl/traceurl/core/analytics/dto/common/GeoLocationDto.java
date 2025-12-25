package com.traceurl.traceurl.core.analytics.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoLocationDto {
    private String country;
    private String region;
    private String city;

    public static GeoLocationDto unknown() {
        return new GeoLocationDto("Unknown", "Unknown", "Unknown");
    }
}