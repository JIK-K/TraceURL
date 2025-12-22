package com.traceurl.traceurl.common.exception;

import com.traceurl.traceurl.common.error.ErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorType errorType;
    private final HttpStatus status;

    public BusinessException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.status = errorType.getStatus();
    }
}
