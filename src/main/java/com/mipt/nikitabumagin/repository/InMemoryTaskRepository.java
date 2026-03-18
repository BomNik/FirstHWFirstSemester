package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.dto.mapper.TaskMapper;
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

/**
 * Primary {@link TaskRepository} implementation that stores tasks in memory using a
 * {@link java.util.concurrent.ConcurrentHashMap}.
 *
 * <p>Thread-safe and suitable for the MVP stage where persistence is not
 * required. An {@link java.util.concurrent.atomic.AtomicLong} sequence generator ensures unique
 * task identifiers.</p>
 *
 * <p>Marked as {@link Primary @Primary} so that it is preferred over any
 * other {@code TaskRepository} bean during autowiring.</p>
 */
@Primary
@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<Long, Task> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);
    private final TaskMapper taskMapper;

    InMemoryTaskRepository(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public Task create(TaskCreateDto request) {
        Long id = idSequence.incrementAndGet();
        Task newTask = taskMapper.toEntity(request);
        newTask.setId(id);
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
    public Task update(Long id, TaskUpdateDto request) {
        if (id == null || !storage.containsKey(id)) {
            throw new TaskNotFoundException(id);
        }

        Task taskToUpdate = storage.get(id);
        Task updated = taskMapper.updateEntity(request, taskToUpdate);
        storage.put(id, updated);

        return updated;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }
}
