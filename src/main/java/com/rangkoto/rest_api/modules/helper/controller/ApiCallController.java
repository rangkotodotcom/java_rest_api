package com.rangkoto.rest_api.modules.helper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rangkoto.rest_api.common.AESHandler;
import com.rangkoto.rest_api.common.FerizySignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/helper/api")
public class ApiCallController {

    private final ObjectMapper objectMapper;
    private final FerizySignature ferizySignature;

    public ApiCallController(ObjectMapper objectMapper, FerizySignature ferizySignature) {
        this.objectMapper = objectMapper;
        this.ferizySignature = ferizySignature;
    }

    @GetMapping("/auth")
    public ResponseEntity<?> auth(
            @Value("${integration.user}") String username,
            @Value("${integration.password}") String password
    ) throws Exception {

        String baseUrl = "https://asdp-api-integration.devops-nutech.com";
        String path = "/auth/global";
        String url = baseUrl + path;
        String method = "POST";
        String aesKey = "base64:BQ8D86bEK4z0te67ls73hR8nuaWWBMC9mFDGKTnGGv8=";
        String aesCipher = "AES-256-CBC";

        // Build payload map
        Map<String, Object> payloadMap = Map.of(
                "username", username,
                "password", password,
                "origin", "https://ferizy.com"
        );

        // Encrypt payload
        AESHandler aesHandler = new AESHandler(aesKey, aesCipher);
        String payload = aesHandler.encrypt(payloadMap);

        // Wrap encrypted payload
        Map<String, Object> bodyMap = Map.of("data", payload);
        String bodyJson = objectMapper.writeValueAsString(bodyMap);

        // Timestamp & nonce
        long timestamp = System.currentTimeMillis() / 1000;
        String nonce = ferizySignature.generateNonce();

        String token = null;

        // Generate signature
        String signature = ferizySignature.generateSignature(
                method, path, bodyMap, timestamp, token, nonce
        );

        // Build request headers map
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");
        requestHeaders.put("Ferizy-Timestamp", String.valueOf(timestamp));
        requestHeaders.put("Ferizy-Nonce", nonce);
        requestHeaders.put("Ferizy-Global-Token", token != null ? "Bearer " + token : "");
        requestHeaders.put("Ferizy-Signature", signature);

        // Build request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        requestHeaders.forEach(builder::header);

        HttpRequest request = builder
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.statusCode());
        result.put("requestHeaders", requestHeaders);
        result.put("requestPayload", bodyMap);
        result.put("responseBody", objectMapper.readValue(response.body(), Object.class));

        return ResponseEntity.status(response.statusCode()).body(result);
    }
}