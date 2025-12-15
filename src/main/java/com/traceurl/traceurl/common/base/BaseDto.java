package com.traceurl.traceurl.common.base;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public abstract class BaseDto {
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}
