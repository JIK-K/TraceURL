package com.traceurl.traceurl.common.constant;

import com.traceurl.traceurl.common.error.ErrorType;
import org.springframework.http.HttpStatus;

public enum UserError implements ErrorType {

    NO_USER("USER-001", "User not found (없는 유저입니다.)", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD("USER-002", "Password is incorrect (비밀번호가 틀렸습니다.)", HttpStatus.BAD_REQUEST),
    DELETED_USER("USER-003", "Already deleted User (탈퇴한 유저입니다.)", HttpStatus.BAD_REQUEST),
    LIMITED_USER("USER-004", "User is restricted (이용제한된 유저입니다.)", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    UserError(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatus getStatus() { return status; }
}
