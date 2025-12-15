package com.traceurl.traceurl.common.dto;

import com.traceurl.traceurl.common.error.ErrorType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Getter
@NoArgsConstructor
public class ResponseDto<T> {

    private Boolean isSuccess;
    private String code;
    private String message;
    private int count;
    private T data;

    public void setFailed(ErrorType error) {
        this.isSuccess = false;
        this.code = error.getCode();
        this.message = error.getMessage();
        this.count = 0;
        this.data = null;
    }

    public void setSuccess(T data) {
        this.isSuccess = true;
        this.code = "";
        this.message = "";
        this.count = (data instanceof Collection<?> c) ? c.size() : 1;
        this.data = data;
    }
}
