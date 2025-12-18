package com.traceurl.traceurl.common.util.string;

import com.traceurl.traceurl.common.enums.ExpireType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class StringUtils {
    public static String generateRandomCode() {
        // 예: 6자리 랜덤 영문+숫자
        return java.util.UUID.randomUUID().toString().substring(0,6);
    }

    public static ExpireType parseExpireType(String expireDate) {
        // expireDate 값에 따라 ExpireType 반환 (예: HOURS, DAYS, DATE)
        if (expireDate == null || expireDate.isBlank()) return ExpireType.NONE;
        // 필요에 따라 구현
        return ExpireType.DATE;
    }

    public static Instant parseExpireAt(String expireDate) {
        if (expireDate == null || expireDate.isBlank()) {
            return null;
        }

        // 1. 프론트에서 온 값은 LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(expireDate);

        // 2. 서버 기준 시간(UTC)으로 간주
        return localDateTime.toInstant(ZoneOffset.UTC);
    }
}
