package com.rangkoto.rest_api.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data // bikin getter + setter + toString + equals/hashcode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String req_id;
    private String srv_id;
    private boolean status;
    private int code;
    private Object error; // masih private, tapi @Data bikin setter/getter
    private T data;
    private String message;
    private long timestamp;
}