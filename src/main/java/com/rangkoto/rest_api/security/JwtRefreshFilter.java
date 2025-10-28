package com.rangkoto.rest_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rangkoto.rest_api.common.ApiResponse;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@NonNullApi
@Component
public class JwtRefreshFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper;
    private final JwtUtil jwtUtil;
    private static final List<String> REFRESH_TOKEN_PATHS = List.of(
            "/auth/refresh"
    );
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtRefreshFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        boolean requiresToken = REFRESH_TOKEN_PATHS.stream()
                .anyMatch(p -> matcher.match(p, request.getRequestURI()));

        if (requiresToken) {
            String token = request.getHeader("x-refresh-token");
            if (token == null || jwtUtil.isInvalidToken(token, JwtUtil.TokenType.REFRESH)) {
                sendError(response);
                return;
            }
            var claims = jwtUtil.extractAllClaims(token, JwtUtil.TokenType.REFRESH);
            request.setAttribute("refresh", claims);
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiResponse<Object> apiResponse = ApiResponse.error(
                3,
                "Invalid or missing refresh token",
                "Unauthorized"
        );

        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
