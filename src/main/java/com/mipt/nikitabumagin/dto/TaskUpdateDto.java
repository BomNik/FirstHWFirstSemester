package com.mipt.nikitabumagin.dto;

import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.validation.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating Task entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for partially updating an existing task")
public class TaskUpdateDto {

    @Schema(description = "New task title", example = "Finish API documentation")
    @Size(min = 3, max = 100, groups = OnUpdate.class)
    @Pattern(regexp = ".*\\S.*", groups = OnUpdate.class)
    private String title;

    @Schema(description = "New task description", example = "Add OpenAPI annotations to all controllers")
    @Size(max = 500, groups = OnUpdate.class)
    private String description;

    @Schema(description = "Updated completion flag", example = "true")
    private Boolean completed;

    @Schema(description = "Updated due date and time", example = "2026-03-27T20:00:00")
    @FutureOrPresent(groups = OnUpdate.class)
    private LocalDateTime dueDate;

    @Schema(description = "Updated task priority", example = "HIGH")
    private Priority priority;

    @Schema(description = "Updated task tags", example = "[\"backend\",\"swagger\"]")
    @Size(max = 5, groups = OnUpdate.class)
    private Set<String> tags;
}
