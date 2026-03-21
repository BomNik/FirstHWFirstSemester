package com.mipt.nikitabumagin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.dto.mapper.TaskMapper;
import com.mipt.nikitabumagin.exception.InvalidTaskException;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.repository.TaskRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class TaskServiceValidationTest {

    private final TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createTask_rejectsDueDateBeforeCreation() {
        TaskService service = createService();
        TaskCreateDto request = new TaskCreateDto(
                "Valid title",
                "Description",
                LocalDateTime.now().minusDays(1),
                Priority.HIGH,
                Set.of("study"));

        InvalidTaskException exception = assertThrows(
                InvalidTaskException.class,
                () -> service.createTask(request));

        assertTrue(exception.getMessage().contains("dueDate"));
    }

    @Test
    void updateTask_rejectsDueDateBeforeCreationAndKeepsStoredTaskUnchanged() {
        TaskService service = createService();
        TaskResponseDto created = service.createTask(new TaskCreateDto(
                "Valid title",
                "Description",
                LocalDateTime.now().plusDays(2),
                Priority.MEDIUM,
                Set.of("study")));

        TaskUpdateDto invalidUpdate = new TaskUpdateDto(
                null,
                null,
                null,
                created.createdAt().minusDays(1),
                null,
                null);

        InvalidTaskException exception = assertThrows(
                InvalidTaskException.class,
                () -> service.updateTask(created.id(), invalidUpdate));

        assertTrue(exception.getMessage().contains("dueDate"));

        TaskResponseDto actual = service.getTaskById(created.id());
        assertEquals(created.dueDate(), actual.dueDate());
        assertEquals(created.createdAt(), actual.createdAt());
    }

    private TaskService createService() {
        TaskService service = new TaskService(new TestTaskRepository(), taskMapper, validator);
        service.initCache();
        return service;
    }

    private static class TestTaskRepository implements TaskRepository {

        private final Map<Long, Task> storage = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Task create(Task task) {
            long id = sequence.incrementAndGet();
            task.setId(id);
            storage.put(id, task);
            return task;
        }

        @Override
        public Optional<Task> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Task> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public Task update(Task task) {
            storage.put(task.getId(), task);
            return task;
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }
}
