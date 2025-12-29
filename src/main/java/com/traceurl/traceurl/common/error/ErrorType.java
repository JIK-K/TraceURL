package com.traceurl.traceurl.common.error;

import com.traceurl.traceurl.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public interface ErrorType {
    String getCode();

    String getMessage();

    HttpStatus getStatus();

    default void throwException() {
        throw new BusinessException(this);
    }
}
