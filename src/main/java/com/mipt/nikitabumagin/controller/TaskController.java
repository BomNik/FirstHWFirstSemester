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
                .header("Location", "/tasks/" + created.getId())
                .body(created);
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable @Min(1) Long id) {
        return taskService.getTaskById(id);
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable @Min(1) Long id, @RequestBody @Valid TaskDto update) {
        return taskService.updateTask(id, update.getTitle(), update.getDescription(),
                update.isCompleted());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable @Min(1) Long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
