package com.rangkoto.rest_api.security;

import com.rangkoto.rest_api.modules.user.dto.UserJwt;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    private final String issuer;

    private final SecretKey globalKey;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    private final long GLOBAL_TOKEN_EXP_MS;
    private final long ACCESS_TOKEN_EXP_MS;
    private final long REFRESH_TOKEN_EXP_MS;

    public JwtUtil(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.global-secret}") String globalSecret,
            @Value("${jwt.access-secret}") String accessSecret,
            @Value("${jwt.refresh-secret}") String refreshSecret,
            @Value("${jwt.global-token-exp-ms:3600000}") long globalTokenExpMs,    // default 1 jam
            @Value("${jwt.access-token-exp-ms:900000}") long accessTokenExpMs,      // default 15 menit
            @Value("${jwt.refresh-token-exp-ms:604800000}") long refreshTokenExpMs  // default 7 hari
    ) {
        this.issuer = issuer;
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
        claims.put("sub", subject);
        return generateToken(subject, claims, GLOBAL_TOKEN_EXP_MS, globalKey);
    }

    /**
     * Generate Access Token
     */
    public String generateAccessToken(String username, UserJwt userJwt) {
        Map<String, Object> claims = new HashMap<>();
        if (userJwt != null) {
            claims.put("email", userJwt.getEmail());
            claims.put("roles", userJwt.getRoles());
            claims.put("user", userJwt);
        }
        return generateToken(username, claims, ACCESS_TOKEN_EXP_MS, accessKey);
    }

    /**
     * Generate Refresh Token
     */
    public String generateRefreshToken(String username, UserJwt userJwt) {
        Map<String, Object> claims = new HashMap<>();
        if (userJwt != null) {
            claims.put("email", userJwt.getEmail());
            claims.put("roles", userJwt.getRoles());
            claims.put("user", userJwt);
        }
        return generateToken(username, claims, REFRESH_TOKEN_EXP_MS, refreshKey);
    }

    /**
     * Generic token generator
     */
    private String generateToken(String subject, Map<String, Object> claims, long expirationMs, SecretKey key) {
        if (claims == null) claims = new HashMap<>();
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setIssuer(issuer)
                .setId(UUID.randomUUID().toString())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extract subject
     */
    public String extractSubject(String token, TokenType type) {
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

    public UserJwt extractUserJwt(String token, TokenType type) {
        SecretKey key = getKey(type);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("user", UserJwt.class);
//        @SuppressWarnings("unchecked")
//        var roles = (java.util.List<String>) claims.get("roles");
//
//        return new UserJwt();
    }

    /**
     * Validate token
     */
    public boolean isInvalidToken(String token, TokenType type) {
        try {
            SecretKey key = getKey(type);
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return false;
        } catch (ExpiredJwtException e) {
            System.out.println(type + " token expired: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println(type + " token invalid: " + e.getMessage());
        }
        return true;
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

    /**
     * Get ExpiresIn berdasarkan token type
     */
    public Long getExpiresIn(TokenType type) {
        return switch (type) {
            case GLOBAL -> GLOBAL_TOKEN_EXP_MS;
            case ACCESS -> ACCESS_TOKEN_EXP_MS;
            case REFRESH -> REFRESH_TOKEN_EXP_MS;
        };
    }

    /**
     * Get ExpiresIn berdasarkan token type
     */
    public Long getExpiresInSeconds(TokenType type) {
        Long expiresInMs = getExpiresIn(type);
        return expiresInMs / 1000;
    }
}
