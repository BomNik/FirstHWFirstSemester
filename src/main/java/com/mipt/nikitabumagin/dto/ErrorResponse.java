package com.mipt.nikitabumagin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Uniform error response used across the API")
public record ErrorResponse(
        @Schema(description = "Timestamp when the error response was generated",
                example = "2026-03-22T09:30:00Z")
        Instant timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP reason phrase", example = "Bad Request")
        String error,
        @Schema(description = "Human-readable error message", example = "Validation failed")
        String message,
        @Schema(description = "Request path that caused the error", example = "/api/tasks")
        String path,
        @Schema(description = "Additional structured error details")
        Map<String, Object> details
) {

}
