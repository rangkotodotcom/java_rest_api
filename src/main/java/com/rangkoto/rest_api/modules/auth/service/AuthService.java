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

        if (payload == null || !payload.containsKey("username") || !payload.containsKey("password")) {
            return Optional.empty();
        }

        String inputUser = (String) payload.get("username");
        String inputPass = (String) payload.get("password");

        if (inputUser == null || inputUser.isBlank() || inputPass == null || inputPass.isBlank()) {
            return Optional.empty();
        }

        boolean isUsernameValid = inputUser.matches("^[a-zA-Z0-9_]+$");
        boolean isPasswordValid = inputPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

        if (!isUsernameValid || !isPasswordValid) {
            return Optional.empty();
        }

        if (username.equals(inputUser) && password.equals(inputPass)) {
            Map<String, Object> auth = new HashMap<>();
            auth.put("usr", inputUser);
            auth.put("chn", "web");
            auth.put("aud", "rangkoto.com");
            return Optional.of(auth);
        }

        return Optional.empty();
    }


//    public Map<String, Object> authWeb(Map<String, Object> payload) {
//        String username = appProperties.getWebUsername();
//        String password = appProperties.getWebPassword();
//
//        // Validasi: payload tidak boleh null
//        if (payload == null) {
//            throw new CustomIllegalArgumentException("Payload tidak boleh null");
//        }
//
//        // Validasi: harus punya username dan password
//        if (!payload.containsKey("username") || !payload.containsKey("password")) {
//            throw new CustomIllegalArgumentException("Payload harus berisi 'username' dan 'password'");
//        }
//
//        String inputUser = (String) payload.get("username");
//        String inputPass = (String) payload.get("password");
//
//        // Validasi nilai kosong/null
//        if (inputUser == null || inputUser.isBlank()) {
//            throw new CustomIllegalArgumentException("Username tidak boleh kosong");
//        }
//
//        if (inputPass == null || inputPass.isBlank()) {
//            throw new CustomIllegalArgumentException("Password tidak boleh kosong");
//        }
//
//        // Validasi format username & password
//        boolean isUsernameValid = inputUser.matches("^[a-zA-Z0-9_]+$");
//        boolean isPasswordValid = inputPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
//
//        if (!isUsernameValid) {
//            throw new CustomIllegalArgumentException("Format username tidak valid (hanya huruf, angka, dan underscore)");
//        }
//
//        if (!isPasswordValid) {
//            throw new CustomIllegalArgumentException("Password harus minimal 8 karakter dan mengandung huruf besar, kecil, dan angka");
//        }
//
//        // Cek kesesuaian kredensial
//        if (!username.equals(inputUser) || !password.equals(inputPass)) {
//            throw new CustomIllegalArgumentException("Username atau password salah");
//        }
//
//        // Kalau valid, buat payload auth
//        Map<String, Object> auth = new HashMap<>();
//        auth.put("username", inputUser);
//        auth.put("channel", "web");
//        auth.put("aud", "rangkoto.com");
//
//        return auth;
//    }


    public Optional<Map<String, Object>> authMobile(Map<String, Object> payload) {
        String username = appProperties.getMobileUsername();
        String password = appProperties.getMobilePassword();

        if (payload == null || !payload.containsKey("username") || !payload.containsKey("password")) {
            return Optional.empty();
        }

        String inputUser = (String) payload.get("username");
        String inputPass = (String) payload.get("password");

        if (inputUser == null || inputUser.isBlank() || inputPass == null || inputPass.isBlank()) {
            return Optional.empty();
        }

        boolean isUsernameValid = inputUser.matches("^[a-zA-Z0-9_]+$");
        boolean isPasswordValid = inputPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

        if (!isUsernameValid || !isPasswordValid) {
            return Optional.empty();
        }

        if (username.equals(inputUser) && password.equals(inputPass)) {
            Map<String, Object> auth = new HashMap<>();
            auth.put("usr", inputUser);
            auth.put("chn", "mobile");
            auth.put("aud", "com.rangkoto.mobile");
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
