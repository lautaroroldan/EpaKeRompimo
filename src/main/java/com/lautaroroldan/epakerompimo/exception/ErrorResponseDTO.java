package com.lautaroroldan.epakerompimo.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details
) {
    public ErrorResponseDTO(Instant timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
}
