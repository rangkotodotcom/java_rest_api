package com.rangkoto.rest_api.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username or email must not be blank")
    @Size(min = 3, max = 100, message = "Username or email must be between 3 and 100 characters")
    private String usernameOrEmail;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters")
    private String password;
}