package com.rangkoto.rest_api.exception;

import lombok.Getter;

@Getter
public class InvalidAuthException extends RuntimeException {
    private final String code;

    public InvalidAuthException(AuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
