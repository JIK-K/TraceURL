package com.traceurl.traceurl.common.exception;

import com.traceurl.traceurl.common.error.ErrorType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BusinessException(ErrorType errorType) {
        super(errorType.getMessage());
        this.code = errorType.getCode();
        this.status = errorType.getStatus();
    }
}
