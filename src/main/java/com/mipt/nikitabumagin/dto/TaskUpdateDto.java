package com.mipt.nikitabumagin.dto;

import com.mipt.nikitabumagin.model.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.mipt.nikitabumagin.validation.OnUpdate;

/**
 * DTO for updating Task entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateDto {

    @Size(min = 3, max = 100, groups = OnUpdate.class)
    @Pattern(regexp = ".*\\S.*", groups = OnUpdate.class)
    private String title;

    @Size(max = 500, groups = OnUpdate.class)
    private String description;

    private Boolean completed;

    @FutureOrPresent(groups = OnUpdate.class)
    private LocalDateTime dueDate;

    private Priority priority;

    @Size(max = 5, groups = OnUpdate.class)
    private Set<String> tags;
}
