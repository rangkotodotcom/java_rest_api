package com.rangkoto.rest_api.modules.helper.dto;

import jakarta.validation.constraints.NotEmpty;

public class DecryptRequest {

    @NotEmpty(message = "'encrypted' field must not be empty")
    private String encrypted;

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }
}
