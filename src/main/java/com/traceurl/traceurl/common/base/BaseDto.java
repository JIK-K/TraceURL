package com.traceurl.traceurl.common.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@SuperBuilder
public abstract class BaseDto {
    protected OffsetDateTime createdAt;
    protected OffsetDateTime updatedAt;
}
