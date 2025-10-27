package com.rangkoto.rest_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import jakarta.servlet.http.HttpServletRequest;

import com.rangkoto.rest_api.common.ApiResponse;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 404 Not Found
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(
                404,
                "Resource not found",
                "Path " + request.getRequestURI() + " not found"
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 403 Forbidden
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbidden(Exception ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(
                403,
                "Forbidden",
                "You do not have permission to access " + request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 405 Method Not Allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        msg.append(ex.getMethod()).append(" method is not supported for this request. Supported methods are: ");
        Objects.requireNonNull(ex.getSupportedHttpMethods()).forEach(t -> msg.append(t).append(" "));
        ApiResponse<Object> response = ApiResponse.error(
                405,
                "Method Not Allowed",
                msg.toString().trim()
        );
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // ================= Validation Errors =================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ApiResponse<Object> response = ApiResponse.error(
                422,
                "Validation Failed",
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ================= Illegal Argument =================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        ApiResponse<Object> response = ApiResponse.error(
                400,
                "Bad Request",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 500 Internal Server Error (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(
                500,
                "Internal server error",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
