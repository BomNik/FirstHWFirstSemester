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

/**
 * DTO for creating Task entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateDto {

    @NotBlank(groups = OnCreate.class)
    @Size(min = 3, max = 100, groups = OnCreate.class)
    private String title;

    @Size(max = 500, groups = OnCreate.class)
    private String description;

    @FutureOrPresent(groups = OnCreate.class)
    private LocalDateTime dueDate;

    @NotNull(groups = OnCreate.class)
    private Priority priority;

    @Size(max = 5, groups = OnCreate.class)
    private Set<String> tags;
}
