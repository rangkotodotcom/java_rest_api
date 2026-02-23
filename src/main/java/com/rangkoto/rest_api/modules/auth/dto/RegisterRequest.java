package com.rangkoto.rest_api.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, max = 64, message = "Password must be between 6 and 64 characters")
    private String password;
}