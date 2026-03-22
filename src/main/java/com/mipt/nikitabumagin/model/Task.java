package com.mipt.nikitabumagin.model;

import com.mipt.nikitabumagin.validation.DueDateNotBeforeCreation;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a single task in the task management system.
 *
 * <p>Each task has a unique {@code id}, a {@code title}, an optional
 * {@code description}, and a {@code completed} flag indicating whether the task has been
 * finished.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DueDateNotBeforeCreation
public class Task {

    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private Priority priority;
    private Set<String> tags;

    public Task(Long id, String title, String description, Boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
    }
}
