package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import com.mipt.nikitabumagin.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<Long, Task> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Task create(String title, String description, Boolean completed) {
        Long id = idSequence.incrementAndGet();
        Task newTask = new Task(id, title, description, completed);
        storage.put(id, newTask);
        return newTask;
    }

    @Override
    public Optional<Task> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Task update(Task task) {
        Long id = task.getId();
        if (id == null || !storage.containsKey(id)) {
            throw new TaskNotFoundException(id);
        }
        storage.put(id, task);
        return task;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }
}
