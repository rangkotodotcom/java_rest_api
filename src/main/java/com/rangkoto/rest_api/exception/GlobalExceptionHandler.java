package com.rangkoto.rest_api.exception;

import com.rangkoto.rest_api.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 403 Forbidden
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbidden(Exception ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(
                403,
                "Forbidden",
                "You do not have permission to access " + request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 405 Method Not Allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        msg.append(ex.getMethod())
                .append(" method is not supported for this request. Supported methods are: ");
        Objects.requireNonNull(ex.getSupportedHttpMethods())
                .forEach(t -> msg.append(t).append(" "));
        ApiResponse<Object> response = ApiResponse.error(
                405,
                "Method Not Allowed",
                msg.toString().trim()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // ================= Validation Errors =================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
                        (existing, replacement) -> existing
                ));
        ApiResponse<Object> response = ApiResponse.error(422, "Validation Failed", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleFieldValidation(FieldValidationException ex) {
        ApiResponse<Object> response = ApiResponse.error(400, "Bad Request", ex.getErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ================= Illegal Argument =================
    @ExceptionHandler(CustomIllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(CustomIllegalArgumentException ex) {
        ApiResponse<Object> response = ApiResponse.error(
                ex.getCode(),
                "Bad Request",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    // public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
    //     // Misal pesan kita formatnya "field: message"
    //     String msg = ex.getMessage();
    //     Map<String, String> errorMap = Map.of("error", msg); // default key "error"

    //     ApiResponse<Object> response = ApiResponse.error(
    //             400,
    //             "Bad Request",
    //             errorMap
    //     );
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    // }

    // ================= Missing @RequestParam =================
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParams(MissingServletRequestParameterException ex) {
        Map<String, String> error = Map.of(
                ex.getParameterName(), "Parameter is required"
        );

        ApiResponse<Object> response = ApiResponse.error(
                400,
                "Bad Request",
                error
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ================= Type Mismatch (Query or Path Variable) =================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> error = Map.of(
                ex.getName(), "Invalid value: " + ex.getValue()
        );

        ApiResponse<Object> response = ApiResponse.error(
                400,
                "Bad Request",
                error
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingRequestBody(HttpMessageNotReadableException ex) {
        Map<String, String> error = Map.of("request_body", "Request body is missing or invalid");
        ApiResponse<Object> response = ApiResponse.error(400, "Bad Request", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 500 Internal Server Error (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.error(500, "Internal server error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
