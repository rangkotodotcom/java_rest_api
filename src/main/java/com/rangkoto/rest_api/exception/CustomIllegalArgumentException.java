package com.rangkoto.rest_api.exception;

import lombok.Getter;

@Getter
public class CustomIllegalArgumentException extends IllegalArgumentException {
    private final int code;

    public CustomIllegalArgumentException(String message, int code) {
        super(message);
        this.code = code;
    }

    public CustomIllegalArgumentException(String message) {
        this(message, 0);
    }

}
