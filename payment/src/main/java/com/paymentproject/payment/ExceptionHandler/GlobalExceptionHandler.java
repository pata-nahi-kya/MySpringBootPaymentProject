package com.paymentproject.payment.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException; // Added this import
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global Exception Handler
 *
 * Without this class, unhandled exceptions in controllers return a Spring Boot
 * default error JSON that includes the full exception message and sometimes a
 * stack trace — both of which leak internal details to clients.
 *
 * This handler intercepts exceptions across all @RestController classes and
 * returns consistent, client-safe error responses while logging the full detail
 * server-side.
 *
 * Response format:
 * {
 *   "timestamp": "2025-01-01T12:00:00Z",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "User not found: 99",
 *   "path": "/bank/user/getMyDetail/99"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle ResponseStatusException — these are thrown explicitly by service and
     * controller layers with a specific HTTP status. The message is controlled by
     * our code so it is safe to return to the client.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {

        log.warn("ResponseStatusException on {}: {} {}", request.getRequestURI(),
                ex.getStatusCode(), ex.getReason());

        return buildResponse(ex.getStatusCode().value(),
                HttpStatus.resolve(ex.getStatusCode().value()),
                ex.getReason(),
                request.getRequestURI());
    }

    /**
     * Handle BadCredentialsException — thrown by Spring Security when login fails.
     * This replaces the massive terminal stack trace with a clean, single-line log.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        // Clean, single-line terminal log
        log.warn("Authentication failed on {}: Invalid username or password.", request.getRequestURI());

        // Structured, standard 401 Unauthorized JSON response for the client
        return buildResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password.",
                request.getRequestURI());
    }

    /**
     * Catch-all handler for unexpected exceptions. Logs the full stack trace
     * server-side but returns a generic 500 message to the client so internal
     * details are not exposed.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            int status, HttpStatus httpStatus, String message, String path) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", httpStatus != null ? httpStatus.getReasonPhrase() : "Error");
        body.put("message", message);
        body.put("path", path);

        return ResponseEntity.status(status).body(body);
    }
}
