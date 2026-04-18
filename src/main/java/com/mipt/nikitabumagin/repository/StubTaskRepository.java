package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * Read-only stub repository preloaded with sample data.
 *
 * <p>Intended for demonstration and testing purposes. Any mutating operations
 * throw {@link UnsupportedOperationException}.</p>
 *
 * <p>After migration to Spring Data JPA this class no longer implements
 * {@link TaskRepository}, because {@code TaskRepository} now extends {@code JpaRepository} and
 * contains a large number of framework methods that should not be manually stubbed.</p>
 */
@Repository
public class StubTaskRepository {

    private final List<Task> tasks;

    public StubTaskRepository() {
        List<Task> seed = new ArrayList<>();

        Task task1 = Task.builder()
                .id(1L)
                .title("Sample task")
                .description("Stub repository task")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.MEDIUM)
                .tags(Set.of("stub"))
                .build();
        seed.add(task1);

        Task task2 = Task.builder()
                .id(2L)
                .title("Done task")
                .description("Completed stub task")
                .completed(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(Priority.HIGH)
                .tags(Set.of("stub", "done"))
                .build();
        seed.add(task2);

        this.tasks = Collections.unmodifiableList(seed);
    }

    public Task create(Task task) {
        throw new UnsupportedOperationException("Stub repository is read-only");
    }

    public Optional<Task> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return tasks.stream()
                .filter(task -> Objects.equals(task.getId(), id))
                .findFirst();
    }

    public List<Task> findAll() {
        return tasks;
    }

    public Task update(Task task) {
        throw new UnsupportedOperationException("Stub repository is read-only");
    }

    public boolean deleteById(Long id) {
        throw new UnsupportedOperationException("Stub repository is read-only");
    }
}
