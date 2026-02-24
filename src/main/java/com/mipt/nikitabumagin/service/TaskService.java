package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.exception.InvalidTaskException;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public TaskRepository getRepository() {
        return repository;
    }

    public Task createTask(String title, String description, Boolean completed) {
        validateTaskFields(title, description, completed);
        return repository.create(title, description, completed);
    }

    public Task getTaskById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
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
        return repository.update(task);
    }

    public void deleteTaskById(Long id) {
        if (!repository.deleteById(id)) {
            throw new TaskNotFoundException(id);
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
