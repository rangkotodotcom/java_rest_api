package com.rangkoto.rest_api.modules.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AuthGlobalRequest {
    @NotNull(message = "'data' field must not be null")
    private String data;
}
