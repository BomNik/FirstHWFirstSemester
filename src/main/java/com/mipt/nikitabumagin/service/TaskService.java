package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.exception.InvalidTaskException;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository repository;
    private Map<Long, Task> taskCache;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void initCache() {
        taskCache = new LinkedHashMap<>();
        try {
            repository.create("Welcome", "First task created on startup", false);
            repository.create("Readme", "Check API endpoints in controller", false);
            repository.create("Done example", "This one is already completed", true);
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
            log.warn("Failed to write cache stats to {}: {}", out.toAbsolutePath(), e.getMessage());
        }

        if (taskCache != null) {
            taskCache.clear();
        }
    }

    public Task createTask(String title, String description, Boolean completed) {
        validateTaskFields(title, description, completed);
        Task created = repository.create(title, description, completed);
        if (created != null && created.getId() != null && taskCache != null) {
            taskCache.put(created.getId(), created);
        }
        return created;
    }

    public Task getTaskById(Long id) {
        if (id == null) {
            throw new TaskNotFoundException(null);
        }
        if (taskCache != null) {
            Task cached = taskCache.get(id);
            if (cached != null) {
                return cached;
            }
        }
        Task task = repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        if (task.getId() != null && taskCache != null) {
            taskCache.put(task.getId(), task);
        }
        return task;
    }

    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    public Task updateTask(Long id, String title, String description, Boolean completed) {
        validateTaskFields(title, description, completed);
        Task task = getTaskById(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed);
        Task updated = repository.update(task);
        if (updated != null && updated.getId() != null && taskCache != null) {
            taskCache.put(updated.getId(), updated);
        }
        return updated;
    }

    public void deleteTaskById(Long id) {
        if (!repository.deleteById(id)) {
            throw new TaskNotFoundException(id);
        }
        if (taskCache != null && id != null) {
            taskCache.remove(id);
        }
    }

    private void validateTaskFields(String title, String description, Boolean completed) {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidTaskException("Title must not be empty");
        }
        if (description == null) {
            throw new InvalidTaskException("Description must not be null");
        }
        if (completed == null) {
            throw new InvalidTaskException("Completed must not be null");
        }
    }
}
