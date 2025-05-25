package com.project.document_qa.exception;

import com.project.document_qa.enums.ResponseCode;
import lombok.Getter;

import java.io.Serial;

public class CommonApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private int code;
    private String message;

    public CommonApiException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
