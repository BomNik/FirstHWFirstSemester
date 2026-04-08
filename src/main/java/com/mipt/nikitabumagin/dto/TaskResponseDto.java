package com.mipt.nikitabumagin.dto;

import com.mipt.nikitabumagin.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for responding with Task entities.
 */
@Schema(description = "Response body with task data")
public record TaskResponseDto(
        @Schema(description = "Task identifier", example = "1")
        Long id,
        @Schema(description = "Task title", example = "Prepare SpringDoc notes")
        String title,
        @Schema(description = "Task description", example = "Document controllers and DTOs")
        String description,
        @Schema(description = "Whether the task is completed", example = "false")
        Boolean completed,
        @Schema(description = "Task creation timestamp", example = "2026-03-22T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Task due date and time", example = "2026-03-25T18:00:00")
        LocalDateTime dueDate,
        @Schema(description = "Task priority", example = "MEDIUM")
        Priority priority,
        @Schema(description = "Task tags", example = "[\"study\",\"api\"]")
        Set<String> tags
) {

}
