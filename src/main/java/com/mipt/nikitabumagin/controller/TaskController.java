package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.dto.TaskDto;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes CRUD endpoints for task management.
 *
 * <p>All request/response bodies are validated using Jakarta Bean Validation.
 * Endpoints:
 * <ul>
 *   <li>{@code POST   /api/tasks}       — create a new task</li>
 *   <li>{@code GET    /api/tasks/{id}}   — retrieve a task by its identifier</li>
 *   <li>{@code GET    /api/tasks}        — list all tasks</li>
 *   <li>{@code PUT    /api/tasks/{id}}   — update an existing task</li>
 *   <li>{@code DELETE /api/tasks/{id}}   — delete a task</li>
 * </ul>
 *
 * @see com.mipt.nikitabumagin.service.TaskService
 */
@RestController
@RequestMapping("/api/tasks")
@Validated
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody @Valid TaskDto request) {
        Task created = taskService.createTask(request.getTitle(),
                request.getDescription(),
                request.isCompleted());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/tasks/" + created.getId())
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable @Min(1) Long id,
            @RequestBody @Valid TaskDto update) {
        return ResponseEntity.ok(
                taskService.updateTask(id, update.getTitle(), update.getDescription(),
                        update.isCompleted()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable @Min(1) Long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
