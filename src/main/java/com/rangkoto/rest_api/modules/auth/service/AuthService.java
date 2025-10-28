package com.rangkoto.rest_api.modules.auth.service;

import com.rangkoto.rest_api.config.AppProperties;
import com.rangkoto.rest_api.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final AppProperties appProperties;
    private final JwtUtil jwtUtil;

    public AuthService(AppProperties appProperties, JwtUtil jwtUtil) {
        this.appProperties = appProperties;
        this.jwtUtil = jwtUtil;
    }

    public Optional<Map<String, Object>> authWeb(Map<String, Object> payload) {
        String username = appProperties.getWebUsername();
        String password = appProperties.getWebPassword();

        String inputUser = (String) payload.get("username");
        String inputPass = (String) payload.get("password");

        if (username.equals(inputUser) && password.equals(inputPass)) {
            Map<String, Object> auth = new HashMap<>();
            auth.put("username", inputUser);
            auth.put("channel", "web");
            return Optional.of(auth);
        }

        return Optional.empty();
    }

    public Optional<Map<String, Object>> authMobile(Map<String, Object> payload) {
        String username = appProperties.getMobileUsername();
        String password = appProperties.getMobilePassword();

        String inputUser = (String) payload.get("username");
        String inputPass = (String) payload.get("password");

        if (username.equals(inputUser) && password.equals(inputPass)) {
            Map<String, Object> auth = new HashMap<>();
            auth.put("username", inputUser);
            auth.put("channel", "mobile");
            return Optional.of(auth);
        }

        return Optional.empty();
    }

    public Map<String, Object> createToken(Map<String, Object> payload) {
        String token = jwtUtil.generateGlobalToken((String) payload.get("username"), payload);
        Long expiresIn = jwtUtil.getExpiresInSeconds(JwtUtil.TokenType.GLOBAL);

        Map<String, Object> res = new HashMap<>();
        res.put("type", "Bearer");
        res.put("global_token", token);
        res.put("expires_in", expiresIn);

        return res;
    }
}
