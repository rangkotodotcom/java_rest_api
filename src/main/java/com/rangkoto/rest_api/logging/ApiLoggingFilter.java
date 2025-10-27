package com.rangkoto.rest_api.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@NonNullApi
@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.nanoTime();
        filterChain.doFilter(wrappedRequest, wrappedResponse);
        long end = System.nanoTime();

        double durationInSeconds = (end - start) / 1_000_000_000.0;

        String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
        String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

        // Normalisasi JSON agar single line
        requestBody = normalizeJson(requestBody);
        responseBody = normalizeJson(responseBody);

        if (!request.getRequestURI().startsWith("/helper")) {
            writeLog(wrappedRequest, wrappedResponse, requestBody, responseBody, durationInSeconds);
        }
        wrappedResponse.copyBodyToResponse();
    }

    private void writeLog(HttpServletRequest request, HttpServletResponse response,
                          String requestBody, String responseBody, double duration) throws IOException {

        LocalDate today = LocalDate.now();
        String folderPath = String.format("logs/%d/%02d", today.getYear(), today.getMonthValue());
        File folder = new File(folderPath);
        boolean createDir = folder.mkdirs();

        String fileName = String.format("log_api_%02d.log", today.getDayOfMonth());
        File logFile = new File(folder, fileName);

        String createdBy = resolveCreatedBy(request);

        String logLine = String.format(
                "%s | %.5f | %s | %s | %s | %d | %s | %s%n",
                Instant.now(),
                duration,
                createdBy,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                requestBody,
                responseBody
        );

        try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
            fos.write(logLine.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String normalizeJson(String json) {
        try {
            JsonNode tree = objectMapper.readTree(json);
            return objectMapper.writeValueAsString(tree); // single line
        } catch (Exception e) {
            // Kalau bukan JSON, kembalikan apa adanya
            return json;
        }
    }

    private String resolveCreatedBy(HttpServletRequest request) {
        // 1. Cek Spring Security authentication (access token)
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null
                && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }

        // 2. Cek global token
        Object globalClaims = request.getAttribute("global");
        if (globalClaims instanceof Map<?, ?> globalMap) {
            Object username = globalMap.get("username");
            if (username != null) return username.toString();
        }

        // 3. Cek refresh token
        Object refreshClaims = request.getAttribute("refresh");
        if (refreshClaims instanceof Map<?, ?> refreshMap) {
            Object username = refreshMap.get("username");
            if (username != null) return username.toString();
        }

        // 4. Default
        return "system";
    }
}
