package com.rangkoto.rest_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rangkoto.rest_api.common.ApiResponse;
import com.rangkoto.rest_api.common.ApiResponseFactory;
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
public class JwtGlobalFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper;

    private final JwtUtil jwtUtil;

    private final ApiResponseFactory responseFactory;

    public JwtGlobalFilter(JwtUtil jwtUtil, ApiResponseFactory responseFactory) {
        this.jwtUtil = jwtUtil;
        this.responseFactory = responseFactory;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    private final AntPathMatcher matcher = new AntPathMatcher();

    private static final List<String> GLOBAL_TOKEN_PATHS = List.of(
            "/landing/**",
            "/auth/login",
            "/auth/register",
            "/auth/forgot-password"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        boolean requiresGlobalToken = GLOBAL_TOKEN_PATHS.stream()
                .anyMatch(p -> matcher.match(p, path));

        if (requiresGlobalToken) {
            String token = request.getHeader("x-global-token");
            if (token == null || jwtUtil.isInvalidToken(token, JwtUtil.TokenType.GLOBAL)) {
                sendError(response);
                return;
            }
            var claims = jwtUtil.extractAllClaims(token, JwtUtil.TokenType.GLOBAL);
            request.setAttribute("global", claims);
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiResponse<Object> apiResponse = responseFactory.error(
                0,
                "Invalid or missing global token",
                "Unauthorized"
        );

        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}
