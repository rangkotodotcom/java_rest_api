package com.rangkoto.rest_api.security;

import com.rangkoto.rest_api.modules.user.dto.UserDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey globalKey;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    private final long GLOBAL_TOKEN_EXP_MS;
    private final long ACCESS_TOKEN_EXP_MS;
    private final long REFRESH_TOKEN_EXP_MS;

    public JwtUtil(
            @Value("${jwt.global-secret}") String globalSecret,
            @Value("${jwt.access-secret}") String accessSecret,
            @Value("${jwt.refresh-secret}") String refreshSecret,
            @Value("${jwt.global-token-exp-ms:3600000}") long globalTokenExpMs,    // default 1 jam
            @Value("${jwt.access-token-exp-ms:900000}") long accessTokenExpMs,      // default 15 menit
            @Value("${jwt.refresh-token-exp-ms:604800000}") long refreshTokenExpMs  // default 7 hari
    ) {
        this.globalKey = Keys.hmacShaKeyFor(globalSecret.getBytes());
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes());
        this.GLOBAL_TOKEN_EXP_MS = globalTokenExpMs;
        this.ACCESS_TOKEN_EXP_MS = accessTokenExpMs;
        this.REFRESH_TOKEN_EXP_MS = refreshTokenExpMs;
    }

    /**
     * Generate Global Token
     */
    public String generateGlobalToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, GLOBAL_TOKEN_EXP_MS, globalKey);
    }

    /**
     * Generate Access Token
     */
    public String generateAccessToken(String username, UserDto userDto) {
        Map<String, Object> claims = new HashMap<>();
        if (userDto != null) {
            claims.put("email", userDto.getEmail());
            claims.put("roles", userDto.getRoles());
        }
        return generateToken(username, claims, ACCESS_TOKEN_EXP_MS, accessKey);
    }

    /**
     * Generate Refresh Token
     */
    public String generateRefreshToken(String username, UserDto userDto) {
        Map<String, Object> claims = new HashMap<>();
        if (userDto != null) {
            claims.put("email", userDto.getEmail());
            claims.put("roles", userDto.getRoles());
        }
        return generateToken(username, claims, REFRESH_TOKEN_EXP_MS, refreshKey);
    }

    /**
     * Generic token generator
     */
    private String generateToken(String subject, Map<String, Object> claims, long expirationMs, SecretKey key) {
        if (claims == null) claims = new HashMap<>();
        return Jwts.builder()
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extract username / subject
     */
    public String extractUsername(String token, TokenType type) {
        return extractAllClaims(token, type).getSubject();
    }

    /**
     * Extract all claims
     */
    public Claims extractAllClaims(String token, TokenType type) {
        SecretKey key = getKey(type);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UserDto extractUserDto(String token, TokenType type) {
        SecretKey key = getKey(type);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.get("email", String.class);
        @SuppressWarnings("unchecked")
        var roles = (java.util.List<String>) claims.get("roles");

        return new UserDto(email, roles);
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token, TokenType type) {
        try {
            SecretKey key = getKey(type);
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println(type + " token expired: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println(type + " token invalid: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check token expired
     */
    public boolean isTokenExpired(String token, TokenType type) {
        try {
            Date expiration = extractAllClaims(token, type).getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Get SecretKey berdasarkan token type
     */
    private SecretKey getKey(TokenType type) {
        return switch (type) {
            case GLOBAL -> globalKey;
            case ACCESS -> accessKey;
            case REFRESH -> refreshKey;
        };
    }

    /**
     * Enum untuk token type
     */
    public enum TokenType {
        GLOBAL,
        ACCESS,
        REFRESH
    }
}
