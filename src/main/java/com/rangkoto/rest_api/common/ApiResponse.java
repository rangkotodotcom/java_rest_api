package com.rangkoto.rest_api.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String req_id; // unique per request
    private String srv_id; // static server id
    private boolean status;
    private int code;
    private Object error; // bisa object atau array of object
    private T data; // bisa object atau list
    private String message;
    private long timestamp;

    // Factory methods
    public static <T> ApiResponse<T> success(int code, T data, String message) {
        return ApiResponse.<T>builder()
                .req_id(UUID.randomUUID().toString().toUpperCase())
                .srv_id("SRV-REST-01")
                .status(true)
                .code(code)
                .data(data)
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    // Success dengan default code 200
    public static <T> ApiResponse<T> success(T data, String message) {
        return success(200, data, message);
    }

    // Error dengan kode custom
    public static <T> ApiResponse<T> error(int code, String message, Object errorDetail) {
        return ApiResponse.<T>builder()
                .req_id(UUID.randomUUID().toString().toUpperCase())
                .srv_id("SRV-REST-01")
                .status(false)
                .code(code)
                .error(errorDetail)
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    // Error dengan default code 400
    public static <T> ApiResponse<T> error(String message, Object errorDetail) {
        return error(400, message, errorDetail);
    }
}
