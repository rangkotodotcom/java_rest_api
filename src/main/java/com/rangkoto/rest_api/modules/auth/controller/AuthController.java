package com.rangkoto.rest_api.modules.auth.controller;

import com.rangkoto.rest_api.common.ApiResponse;
import com.rangkoto.rest_api.common.ApiResponseFactory;
import com.rangkoto.rest_api.modules.auth.dto.AuthGlobalRequest;
import com.rangkoto.rest_api.modules.auth.service.AuthService;
import com.rangkoto.rest_api.modules.helper.service.HelperAESService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final HelperAESService helperAESService;
    private final ApiResponseFactory responseFactory;

    public AuthController(AuthService authService, HelperAESService helperAESService, ApiResponseFactory responseFactory) {
        this.authService = authService;
        this.helperAESService = helperAESService;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/web")
    public ResponseEntity<ApiResponse<?>> loginWeb(@Valid @RequestBody AuthGlobalRequest authGlobalRequest) throws Exception {
        Map<String, Object> payload = helperAESService.decryptData(authGlobalRequest.getData());

        if (payload.isEmpty()) {
            ApiResponse<Object> apiResponse = responseFactory.error(
                    0,
                    "Invalid data credential",
                    null
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        Optional<Map<String, Object>> authResult = authService.authWeb(payload);

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
    public ResponseEntity<ApiResponse<?>> loginMobile(@Valid @RequestBody AuthGlobalRequest authGlobalRequest) throws Exception {
        Map<String, Object> payload = helperAESService.decryptData(authGlobalRequest.getData());

        if (payload.isEmpty()) {
            ApiResponse<Object> apiResponse = responseFactory.error(
                    0,
                    "Invalid data credential",
                    null
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        Optional<Map<String, Object>> authResult = authService.authMobile(payload);

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
}
