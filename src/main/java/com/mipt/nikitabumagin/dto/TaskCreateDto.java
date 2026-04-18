package com.mipt.nikitabumagin.dto;

import com.mipt.nikitabumagin.model.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.mipt.nikitabumagin.validation.OnCreate;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for creating Task entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new task")
public class TaskCreateDto {

    @Schema(description = "Short task title", example = "Prepare SpringDoc notes")
    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 100, groups = OnCreate.class)
    private String title;

    @Schema(description = "Optional detailed task description", example = "Document all task endpoints in Swagger UI")
    @Size(max = 500, groups = OnCreate.class)
    private String description;

    @Schema(description = "Due date and time for the task", example = "2026-03-25T18:00:00")
    @FutureOrPresent(groups = OnCreate.class)
    private LocalDateTime dueDate;

    @Schema(description = "Task priority", example = "MEDIUM")
    @NotNull(groups = OnCreate.class)
    private Priority priority;

    @Schema(description = "Tags assigned to the task", example = "[\"study\",\"api\"]")
    @Size(max = 5, groups = OnCreate.class)
    private Set<String> tags;
}
