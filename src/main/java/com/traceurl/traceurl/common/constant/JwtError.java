package com.traceurl.traceurl.common.constant;

import com.traceurl.traceurl.common.error.ErrorType;
import org.springframework.http.HttpStatus;

public enum JwtError implements ErrorType {

    INVALID_TOKEN("JWT-001", "Invalid JWT Token (유효하지 않은 JWT 토큰입니다.)", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("JWT-002", "Expired JWT Token (만료된 JWT 토큰입니다.)", HttpStatus.UNAUTHORIZED),
    MALFORMED_JWT_EXCEPTION("JWT-003", "Malformed JWT Exception (JWT 구조가 잘못되었습니다.)", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_JWT_EXCEPTION("JWT-004", "Unsupported JWT Token (지원되지 않는 JWT 토큰입니다.)", HttpStatus.UNAUTHORIZED),
    ILLEGAL_ARGUMENT_JWT("JWT-005", "Illegal JWT Argument (JWT 인자가 잘못되었습니다.)", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("JWT-006", "Invalid Refresh Token (유효하지 않은 리프레시 토큰입니다.)", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

    JwtError(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatus getStatus() { return status; }
}
