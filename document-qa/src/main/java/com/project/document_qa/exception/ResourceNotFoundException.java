package com.project.document_qa.exception;

import com.project.document_qa.enums.ResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -77886567465646L;

    private final int code;

    public ResourceNotFoundException(ResponseCode responseCode) {
        this.code = responseCode.getCode();
    }

}