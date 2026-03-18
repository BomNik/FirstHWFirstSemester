package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.model.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Read-only stub implementation of {@link TaskRepository} preloaded with sample data.
 *
 * <p>Intended for demonstration and testing purposes. Any mutating operations
 * ({@code create}, {@code update}, {@code deleteById}) throw
 * {@link UnsupportedOperationException}.</p>
 *
 * <p>Registered as a named bean ({@code "stubTaskRepository"}) via
 * {@link com.mipt.nikitabumagin.config.TaskRepositoryConfig @Configuration} so it can
 * be explicitly injected alongside the primary repository using
 * {@code @Qualifier("stubTaskRepository")}.</p>
 */
public class StubTaskRepository implements TaskRepository {

    private final List<Task> tasks;

    public StubTaskRepository() {
        List<Task> seed = new ArrayList<>();

        Task task1 = new Task(1L, "Sample task", "Stub repository task", false);
        seed.add(task1);

        Task task2 = new Task(2L, "Done task", "Completed stub task", true);
        seed.add(task2);

        this.tasks = Collections.unmodifiableList(seed);
    }

    @Override
    public Task create(TaskCreateDto request) {
        throw new UnsupportedOperationException("Stub repository is read-only");
    }

    @Override
    public Optional<Task> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return tasks.stream()
                .filter(task -> Objects.equals(task.getId(), id))
                .findFirst();
    }

    @Override
    public List<Task> findAll() {
        return tasks;
    }

    @Override
    public Task update(Long id, TaskUpdateDto request) {
        throw new UnsupportedOperationException("Stub repository is read-only");
    }

    @Override
    public boolean deleteById(Long id) {
        throw new UnsupportedOperationException("Stub repository is read-only");
    }
}
