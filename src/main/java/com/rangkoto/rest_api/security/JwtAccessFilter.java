package com.rangkoto.rest_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rangkoto.rest_api.common.ApiResponse;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
public class JwtAccessFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper;
    private final JwtUtil jwtUtil;
    private static final List<String> ACCESS_TOKEN_PATHS = List.of(
            "/book/**",
            "/user/**"
            // tambahkan semua endpoint yang butuh login
    );
    private final AntPathMatcher matcher = new AntPathMatcher();

    public JwtAccessFilter(JwtUtil jwtUtil) {
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

        boolean requiresToken = ACCESS_TOKEN_PATHS.stream()
                .anyMatch(p -> matcher.match(p, request.getRequestURI()));

        if (requiresToken) {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                sendError(response, "Missing access token");
                return;
            }
            token = token.substring(7);
            if (jwtUtil.isInvalidToken(token, JwtUtil.TokenType.ACCESS)) {
                sendError(response, "Invalid access token");
                return;
            }

            var userJwt = jwtUtil.extractUserJwt(token, JwtUtil.TokenType.ACCESS);
            var claims = jwtUtil.extractAllClaims(token, JwtUtil.TokenType.ACCESS);
            request.setAttribute("user", userJwt);
            request.setAttribute("access", claims);

            // Buat Authentication token
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userJwt,       // principal
                            null,          // credentials (tidak perlu)
                            userJwt.getRoles().stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .toList()   // authorities
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set ke SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }


        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiResponse<Object> apiResponse = ApiResponse.error(
                2,
                message,
                "Unauthorized"
        );

        response.getWriter().write(mapper.writeValueAsString(apiResponse));
    }
}

