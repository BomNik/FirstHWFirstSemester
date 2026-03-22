package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.dto.mapper.TaskMapper;
import com.mipt.nikitabumagin.exception.InvalidTaskException;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Core service encapsulating business logic for task management.
 *
 * <p>Delegates persistence to a {@link com.mipt.nikitabumagin.repository.TaskRepository}
 * and maintains an in-memory cache ({@link java.util.LinkedHashMap}) for fast lookups by task
 * identifier.</p>
 *
 * <p>Lifecycle hooks:
 * <ul>
 *   <li>{@link jakarta.annotation.PostConstruct @PostConstruct} — pre-populates the
 *       repository with sample data and warms the cache.</li>
 *   <li>{@link jakarta.annotation.PreDestroy @PreDestroy} — logs cache statistics
 *       and optionally persists them to a file before shutdown.</li>
 * </ul>
 *
 * @see com.mipt.nikitabumagin.repository.TaskRepository
 */
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private Map<Long, Task> taskCache;

    private final TaskRepository repository;
    private final TaskMapper taskMapper;
    private final Validator validator;

    public TaskService(TaskRepository repository, TaskMapper taskMapper, Validator validator) {
        this.repository = repository;
        this.taskMapper = taskMapper;
        this.validator = validator;
    }

    @PostConstruct
    public void initCache() {
        taskCache = new LinkedHashMap<>();
        try {
            repository.create(buildNewTask(new TaskCreateDto(
                    "Welcome",
                    "First task created on startup",
                    LocalDateTime.now().plusDays(1),
                    Priority.MEDIUM,
                    Set.of("startup"))));
            repository.create(buildNewTask(new TaskCreateDto(
                    "Readme",
                    "Check API endpoints in controller",
                    LocalDateTime.now().plusDays(2),
                    Priority.LOW,
                    Set.of("docs", "api"))));
            repository.create(buildNewTask(new TaskCreateDto(
                    "Done example",
                    "This one is already completed",
                    LocalDateTime.now().plusDays(3),
                    Priority.HIGH,
                    Set.of("example"))));
        } catch (RuntimeException e) {
            log.debug("Preload tasks skipped/failed: {}", e.getMessage());
        }

        for (Task task : repository.findAll()) {
            if (task == null) {
                continue;
            }
            if (task.getId() == null) {
                log.warn("Skipping task without id during cache init: title='{}'", task.getTitle());
                continue;
            }
            taskCache.put(task.getId(), task);
        }

        log.info("Task cache initialized: {} entries", taskCache.size());
    }

    @PreDestroy
    public void clearCache() {
        int cacheSize = (taskCache == null) ? 0 : taskCache.size();
        log.info("Destroying TaskService. Cache size before destroy: {}", cacheSize);

        // Optional: persist simple shutdown stats.
        // We write to working directory, so it won't require extra configuration.
        Path out = Path.of("task-cache-stats.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(
                out,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(Instant.now() + " cacheSize=" + cacheSize);
            writer.newLine();
        } catch (IOException e) {
            log.error("Failed to write cache stats to {}: {}", out.toAbsolutePath(),
                    e.getMessage());
        }

        if (taskCache != null) {
            taskCache.clear();
        }
    }

    public TaskResponseDto createTask(TaskCreateDto request) {
        Task task = buildNewTask(request);
        validateTask(task);
        Task created = repository.create(task);
        TaskResponseDto response = taskMapper.toResponseDto(created);
        taskCache.put(created.getId(), created);
        log.info("Task created: {} entries", taskCache.size());
        return response;
    }

    public TaskResponseDto getTaskById(Long id) {
        Task cached = taskCache.get(id);
        if (cached != null) {
            return taskMapper.toResponseDto(cached);
        }

        Task task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        if (task.getId() != null) {
            taskCache.put(task.getId(), task);
        }
        return taskMapper.toResponseDto(task);
    }

    public List<TaskResponseDto> getAllTasks() {
        return repository.findAll().stream().map(taskMapper::toResponseDto).toList();
    }

    public TaskResponseDto updateTask(Long id, TaskUpdateDto request) {
        Task currentTask = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        Task taskToUpdate = copyTask(currentTask);
        taskMapper.updateEntity(request, taskToUpdate);
        validateTask(taskToUpdate);
        Task updated = repository.update(taskToUpdate);
        taskCache.put(updated.getId(), updated);
        log.info("Task updated: {} entries", taskCache.size());
        return taskMapper.toResponseDto(updated);
    }

    public void deleteTaskById(Long id) {
        if (!repository.deleteById(id)) {
            throw new TaskNotFoundException(id);
        }
        log.info("Task deleted: {} entries", taskCache.size());
        taskCache.remove(id);
    }

    private Task buildNewTask(TaskCreateDto request) {
        Task task = taskMapper.toEntity(request);
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    private void validateTask(Task task) {
        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new InvalidTaskException(message);
        }
    }

    private Task copyTask(Task task) {
        return new Task(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCompleted(),
                task.getCreatedAt(),
                task.getDueDate(),
                task.getPriority(),
                task.getTags() == null ? null : Set.copyOf(task.getTags()));
    }
}
