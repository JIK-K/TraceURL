package com.traceurl.traceurl.common.exception;

import com.traceurl.traceurl.common.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseDto<Void>> handleBusinessException(
            BusinessException e
    ) {
        ResponseDto<Void> response = new ResponseDto<>();
        response.setFailed(e.getErrorType());

        return ResponseEntity
                .status(e.getStatus())
                .body(response);
    }
}
