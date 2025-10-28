package com.rangkoto.rest_api.modules.auth.controller;

import com.rangkoto.rest_api.common.ApiResponse;
import com.rangkoto.rest_api.modules.auth.dto.LoginRequest;
import com.rangkoto.rest_api.modules.user.model.User;
import com.rangkoto.rest_api.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth/user")
public class AuthUserController {
    private final UserService userService;

    public AuthUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userService.authenticate(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
        );

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null); // ⚠️ Jangan kirim password ke client!

            ApiResponse<User> apiResponse = ApiResponse.success(
                    user,
                    "Login successful"
            );

            return ResponseEntity.ok(apiResponse);
        }

        ApiResponse<Object> apiResponse = ApiResponse.error(
                101, // kode 401
                "Invalid username/email or password",
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}
