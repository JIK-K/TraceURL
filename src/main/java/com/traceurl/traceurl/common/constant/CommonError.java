package com.traceurl.traceurl.common.constant;

import com.traceurl.traceurl.common.error.ErrorType;
import org.springframework.http.HttpStatus;

public enum CommonError implements ErrorType {

    INVALID_INPUT_VALUE("COMMON-001", "Invalid Input Value (유효하지 않은 입력값입니다.)", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("COMMON-002", "Method Not Allowed (허용되지 않은 메서드입니다.)", HttpStatus.METHOD_NOT_ALLOWED),
    ENTITY_NOT_FOUND("COMMON-003", "Entity Not Found (엔티티를 찾을 수 없습니다.)", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("COMMON-004", "Internal Server Error (서버 내부 오류입니다.)", HttpStatus.INTERNAL_SERVER_ERROR),
    DUPLICATE_ERROR("COMMON-005", "Duplicate Data (이미 존재하는 데이터입니다)", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("COMMON-006", "Access Denied (접근이 거부되었습니다.)", HttpStatus.FORBIDDEN),
    BAD_REQUEST("COMMON-009", "Bad Request (잘못된 요청입니다.)", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELDS("COMMON-010", "Missing Required Fields (필수 필드가 누락되었습니다.)", HttpStatus.BAD_REQUEST),
    DATA_NOT_FOUND("COMMON-011", "Data Not Found (데이터를 찾을 수 없습니다.)", HttpStatus.NOT_FOUND),
    NULL_POINTER("COMMON-012", "Null Pointer Exception (널 포인터 예외가 발생했습니다.)", HttpStatus.INTERNAL_SERVER_ERROR),
    ILLEGAL_ARGUMENT("COMMON-013", "Illegal Argument Exception (잘못된 인자 값입니다.)", HttpStatus.BAD_REQUEST),
    METHOD_ARGUMENT_INVALID("COMMON-014", "Method Argument Not Valid (메서드 인자가 유효하지 않습니다.)", HttpStatus.BAD_REQUEST),
    NO_HANDLER_FOUND("COMMON-015", "No Handler Found (핸들러를 찾을 수 없습니다.)", HttpStatus.NOT_FOUND),
    HTTP_MESSAGE_NOT_READABLE("COMMON-016", "HTTP Message Not Readable (읽을 수 없는 HTTP 메시지입니다.)", HttpStatus.BAD_REQUEST),
    JSON_PROCESSING_ERROR("COMMON-017", "Error During JSON Processing (JSON 처리 중 오류가 발생했습니다.)", HttpStatus.BAD_REQUEST),
    INVALID_PROHIBITED_WORD("COMMON-018", "금지된 단어가 포함되어 있습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    CommonError(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatus getStatus() { return status; }
}
