package com.rangkoto.rest_api.common;

import com.rangkoto.rest_api.config.ServerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApiResponseFactory {

    private final ServerConfig serverConfig;

    public <T> ApiResponse<T> success(int code, T data, String message) {
        return ApiResponse.<T>builder()
                .req_id(UUID.randomUUID().toString().toUpperCase())
                .srv_id(serverConfig.getServerId())
                .status(true)
                .code(code)
                .data(data)
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    public <T> ApiResponse<T> success(T data, String message) {
        return success(200, data, message);
    }

    public <T> ApiResponse<T> error(int code, String message, Object errorDetail) {
        return ApiResponse.<T>builder()
                .req_id(UUID.randomUUID().toString().toUpperCase())
                .srv_id(serverConfig.getServerId())
                .status(false)
                .code(code)
                .error(errorDetail)
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    public <T> ApiResponse<T> error(String message, Object errorDetail) {
        return error(400, message, errorDetail);
    }
}
