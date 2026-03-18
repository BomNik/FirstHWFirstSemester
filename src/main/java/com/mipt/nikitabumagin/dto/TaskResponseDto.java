package com.mipt.nikitabumagin.dto;

import com.mipt.nikitabumagin.model.Priority;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for responding with Task entities.
 */
public record TaskResponseDto(
        Long id,
        String title,
        String description,
        Boolean completed,
        LocalDateTime createdAt,
        LocalDateTime dueDate,
        Priority priority,
        Set<String> tags
) {

}
