package com.lautaroroldan.epakerompimo.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponseDTO> handleUpstreamResponse(RestClientResponseException ex,
                                                                   HttpServletRequest request) {
        log.warn("Upstream API returned {} for {}: {}", ex.getStatusCode(), request.getRequestURI(),
                ex.getResponseBodyAsString());
        return build(HttpStatus.BAD_GATEWAY, "Upstream API error",
                "The external API responded with status " + ex.getStatusCode(), request, null);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponseDTO> handleUpstream(RestClientException ex, HttpServletRequest request) {
        log.error("Could not reach upstream API for {}", request.getRequestURI(), ex);
        return build(HttpStatus.BAD_GATEWAY, "Upstream API unavailable",
                "The external API could not be reached", request, null);
    }

    @ExceptionHandler(ApiClientException.class)
    public ResponseEntity<ErrorResponseDTO> handleApiClient(ApiClientException ex, HttpServletRequest request) {
        log.error("API client error for {}", request.getRequestURI(), ex);
        return build(HttpStatus.BAD_GATEWAY, "Upstream API error", ex.getMessage(), request, null);
    }

    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponseDTO> handleValidation(Exception ex, HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        if (ex instanceof ConstraintViolationException violation) {
            violation.getConstraintViolations().forEach(v ->
                    details.put(v.getPropertyPath().toString(), v.getMessage()));
        } else if (ex instanceof MethodArgumentNotValidException notValid) {
            notValid.getBindingResult().getFieldErrors().forEach(fe ->
                    details.put(fe.getField(), fe.getDefaultMessage()));
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "One or more parameters are invalid",
                request, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String error, String message,
                                                   HttpServletRequest request, Map<String, String> details) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                Instant.now(), status.value(), error, message, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
