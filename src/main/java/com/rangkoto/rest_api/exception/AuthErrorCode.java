package com.rangkoto.rest_api.exception;

import lombok.Getter;

@Getter
public enum AuthErrorCode {
    MISSING_PAYLOAD("AUTH_001", "Payload tidak boleh null"),
    MISSING_FIELDS("AUTH_002", "Payload harus berisi 'username' dan 'password'"),
    EMPTY_USERNAME("AUTH_003", "Username tidak boleh kosong"),
    EMPTY_PASSWORD("AUTH_004", "Password tidak boleh kosong"),
    INVALID_USERNAME_FORMAT("AUTH_005", "Format username tidak valid (hanya huruf, angka, dan underscore)"),
    INVALID_PASSWORD_FORMAT("AUTH_006", "Password harus minimal 8 karakter dan mengandung huruf besar, kecil, dan angka"),
    INVALID_CREDENTIALS("AUTH_007", "Username atau password salah");

    private final String code;
    private final String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
