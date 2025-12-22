package com.traceurl.traceurl.common.constant;

import com.traceurl.traceurl.common.error.ErrorType;
import org.springframework.http.HttpStatus;
public enum FileError implements ErrorType {

    UNSUPPORTED_FILE_TYPE("FILE-001", "Not Valid File Type (잘못된 파일 형식입니다.)", HttpStatus.BAD_REQUEST),
    CONVERSION_FAILED("FILE-002", "파일 변환에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND("FILE-003", "존재하지 않는 파일입니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    FileError(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatus getStatus() { return status; }
}
