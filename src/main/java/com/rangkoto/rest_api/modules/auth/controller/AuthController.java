package com.rangkoto.rest_api.modules.auth.controller;

import com.rangkoto.rest_api.common.ApiResponse;
import com.rangkoto.rest_api.common.ApiResponseFactory;
import com.rangkoto.rest_api.modules.auth.dto.AuthGlobalRequest;
import com.rangkoto.rest_api.modules.auth.dto.LoginRequest;
import com.rangkoto.rest_api.modules.auth.dto.RegisterRequest;
import com.rangkoto.rest_api.modules.auth.service.AuthService;
import com.rangkoto.rest_api.modules.helper.service.HelperAESService;
import com.rangkoto.rest_api.modules.user.dto.UserJwt;
import com.rangkoto.rest_api.modules.user.model.User;
import com.rangkoto.rest_api.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final HelperAESService helperAESService;
    private final ApiResponseFactory responseFactory;

    public AuthController(AuthService authService, UserService userService, HelperAESService helperAESService, ApiResponseFactory responseFactory) {
        this.authService = authService;
        this.userService = userService;
        this.helperAESService = helperAESService;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/web")
    public ResponseEntity<ApiResponse<?>> authWeb(@Valid @RequestBody AuthGlobalRequest authGlobalRequest) throws Exception {
        Map<String, Object> payload = helperAESService.decryptData(authGlobalRequest.getData());

        if (payload.isEmpty()) {
            ApiResponse<Object> apiResponse = responseFactory.error(
                    0,
                    "Invalid data credential",
                    null
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        Optional<Map<String, Object>> authResult = authService.authWebService(payload);

        if (authResult.isEmpty()) {
            ApiResponse<Map<String, String>> apiResponse = responseFactory.error(
                    1,
                    null,
                    "Invalid data credential"
            );
            return ResponseEntity.ok(apiResponse);
        }

        Map<String, Object> res = authService.createTokenGlobal(authResult.get());

        ApiResponse<Map<String, Object>> apiResponse = responseFactory.success(res, "Success");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/mobile")
    public ResponseEntity<ApiResponse<?>> authMobile(@Valid @RequestBody AuthGlobalRequest authGlobalRequest) throws Exception {
        Map<String, Object> payload = helperAESService.decryptData(authGlobalRequest.getData());

        if (payload.isEmpty()) {
            ApiResponse<Object> apiResponse = responseFactory.error(
                    0,
                    "Invalid data credential",
                    null
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        Optional<Map<String, Object>> authResult = authService.authMobileService(payload);

        if (authResult.isEmpty()) {
            ApiResponse<Map<String, String>> apiResponse = responseFactory.error(
                    1,
                    null,
                    "Invalid data credential"
            );
            return ResponseEntity.ok(apiResponse);
        }

        Map<String, Object> res = authService.createTokenGlobal(authResult.get());

        ApiResponse<Map<String, Object>> apiResponse = responseFactory.success(res, "Success");
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> authRegister(@Valid @RequestBody RegisterRequest registerRequest) {

        if (userService.isEmailTaken(registerRequest.getEmail())) {
            ApiResponse<Object> apiResponse = responseFactory.error(
                    0,
                    "Email is already taken",
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }

        try {
            User newUser = userService.register(
                    registerRequest.getName(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword()
            );

            newUser.setPassword(null);

            ApiResponse<User> apiResponse = responseFactory.success(newUser, "Register successful");
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            ApiResponse<Object> apiResponse = responseFactory.error(
                    0,
                    "Failed to register user: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> authLogin(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
        Optional<User> userOpt = userService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(null);

            UserJwt userJwt = new UserJwt();
            userJwt.setId(user.getId().toString());
            userJwt.setName(user.getName());
            userJwt.setEmail(user.getEmail());
            userJwt.setRoles(user.getRoles());

            Map<String, Object> tokenAccess = authService.createTokenAccess(userJwt);
            Map<String, Object> tokenRefresh = authService.createTokenRefresh(userJwt);
            Map<String, Object> token = new HashMap<>();
            token.put("type", tokenAccess.get("type"));
            token.put("access_token", tokenAccess.get("access_token"));
            token.put("refresh_token", tokenRefresh.get("refresh_token"));
            token.put("expires_in", tokenAccess.get("expires_in"));

            Map<String, Object> res = new HashMap<>();
            res.put("user", user);
            res.put("token", token);

            ApiResponse<Map<String, Object>> apiResponse = responseFactory.success(
                    res,
                    "Login successful"
            );

            return ResponseEntity.ok(apiResponse);
        }

        ApiResponse<Object> apiResponse = responseFactory.error(
                101,
                "Invalid email or password",
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}
