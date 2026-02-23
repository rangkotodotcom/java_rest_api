package com.rangkoto.rest_api.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Component
public class FerizySignature {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SecureRandom secureRandom = new SecureRandom();

    private final String defaultKey;

    public FerizySignature(@Value("${integration.key}") String defaultKey) {
        this.defaultKey = defaultKey;
    }

    /**
     * Generate a random nonce of given length (default 16)
     */
    public String generateNonce(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);

        String base64 = Base64.getEncoder().encodeToString(bytes);
        return base64.replaceAll("[/+=]", "");
    }

    public String generateNonce() {
        return generateNonce(32);
    }

    /**
     * Extract signature key from JWT token or fallback to defaultKey
     */
    public String getSignatureKey(String token) {
        if (token == null || token.isEmpty()) return defaultKey;

        String[] parts = token.split("\\.");
        if (parts.length < 2) return defaultKey;

        try {
            String payloadPart = parts[1];
            int padding = 4 - (payloadPart.length() % 4);
            if (padding < 4) payloadPart += "=".repeat(padding);

            payloadPart = payloadPart.replace('-', '+').replace('_', '/');

            byte[] decoded = Base64.getDecoder().decode(payloadPart);
            String json = new String(decoded, StandardCharsets.UTF_8);

            Map<String, Object> payload = objectMapper.readValue(
                    json, new TypeReference<Map<String, Object>>() {
                    }
            );

            return (String) payload.getOrDefault("jti", defaultKey);
        } catch (Exception e) {
            return defaultKey;
        }
    }

    /**
     * Generate HMAC SHA-512 signature
     *
     * @param method    HTTP method (GET, POST, etc.)
     * @param url       API endpoint path
     * @param body      JSON body as Map
     * @param timestamp Current timestamp in milliseconds
     * @param token     Optional JWT token
     * @param nonce     Nonce string
     * @return signature in hex format
     */
    public String generateSignature(String method, String url, Map<String, Object> body, long timestamp, String token, String nonce) throws Exception {
        String bodyJson = (body != null && !body.isEmpty()) ? objectMapper.writeValueAsString(body) : "{}";
        String key = getSignatureKey(token);
        String payload = method.toUpperCase() + "\n" + url + "\n" + (bodyJson != null ? bodyJson : "{}") + "\n" + timestamp + "\n" + nonce;

        byte[] keyBytes = key.startsWith("base64:") ?
                Base64.getDecoder().decode(key.substring(7)) :
                key.getBytes(StandardCharsets.UTF_8);

        //noinspection SpellCheckingInspection
        Mac mac = Mac.getInstance("HmacSHA512");
        //noinspection SpellCheckingInspection
        mac.init(new SecretKeySpec(keyBytes, "HmacSHA512"));
        byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        // Convert to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : signatureBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}