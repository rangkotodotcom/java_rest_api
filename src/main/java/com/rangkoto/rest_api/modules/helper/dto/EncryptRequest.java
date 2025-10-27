package com.rangkoto.rest_api.modules.helper.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class EncryptRequest {

    @NotNull(message = "'data' field must not be null")
    private Map<String, Object> data;

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
