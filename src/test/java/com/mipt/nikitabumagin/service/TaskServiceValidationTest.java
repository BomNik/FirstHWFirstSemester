package com.mipt.nikitabumagin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.dto.mapper.TaskMapper;
import com.mipt.nikitabumagin.exception.InvalidTaskException;
import com.mipt.nikitabumagin.exception.TaskBulkOperationException;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TaskServiceValidationTest {

    private final TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final Map<Long, Task> storage = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Mock
    private TaskRepository repository;

    private TaskService service;

    @BeforeEach
    void setUp() {
        mockRepositoryBackedByInMemoryMap();
        service = new TaskService(repository, taskMapper, validator);
        service.initCache();
    }

    @Test
    void createTask_persistsAndReturnsTaskWithGeneratedId() {
        TaskCreateDto request = new TaskCreateDto(
                "Valid title",
                "Description",
                LocalDateTime.now().plusDays(2),
                Priority.HIGH,
                Set.of("study"));

        TaskResponseDto created = service.createTask(request);

        assertNotNull(created.id());
        assertEquals("Valid title", created.title());
        assertEquals(Priority.HIGH, created.priority());

        TaskResponseDto loaded = service.getTaskById(created.id());
        assertEquals(created.id(), loaded.id());
        assertEquals(created.title(), loaded.title());
    }

    @Test
    void createTask_rejectsDueDateBeforeCreation() {
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
                created.createdAt().minusDays(2),
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

    @Test
    void bulkCompleteTasks_marksAllProvidedTasksAsCompleted() {
        TaskResponseDto first = service.createTask(new TaskCreateDto(
                "First",
                "Description",
                LocalDateTime.now().plusDays(2),
                Priority.LOW,
                Set.of("one")));
        TaskResponseDto second = service.createTask(new TaskCreateDto(
                "Second",
                "Description",
                LocalDateTime.now().plusDays(3),
                Priority.MEDIUM,
                Set.of("two")));

        service.bulkCompleteTasks(List.of(first.id(), second.id()));

        assertTrue(service.getTaskById(first.id()).completed());
        assertTrue(service.getTaskById(second.id()).completed());
    }

    @Test
    void bulkCompleteTasks_throwsWhenAnyTaskIdIsMissingAndDoesNotChangeExistingTasks() {
        TaskResponseDto existing = service.createTask(new TaskCreateDto(
                "Existing",
                "Description",
                LocalDateTime.now().plusDays(2),
                Priority.LOW,
                Set.of("one")));

        assertThrows(
                TaskBulkOperationException.class,
                () -> service.bulkCompleteTasks(List.of(existing.id(), 999_999L)));

        assertFalse(service.getTaskById(existing.id()).completed());
    }

    @Test
    void deleteTaskById_removesTaskAndSubsequentLookupFails() {
        TaskResponseDto created = service.createTask(new TaskCreateDto(
                "To delete",
                "Description",
                LocalDateTime.now().plusDays(1),
                Priority.MEDIUM,
                Set.of("delete")));

        service.deleteTaskById(created.id());

        assertThrows(TaskNotFoundException.class, () -> service.getTaskById(created.id()));
    }

    private void mockRepositoryBackedByInMemoryMap() {
        when(repository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            if (task.getId() == null) {
                task.setId(sequence.incrementAndGet());
            }
            storage.put(task.getId(), task);
            return task;
        });

        when(repository.saveAll(anyIterable())).thenAnswer(invocation -> {
            Iterable<Task> tasks = invocation.getArgument(0);
            List<Task> saved = new ArrayList<>();
            for (Task task : tasks) {
                if (task.getId() == null) {
                    task.setId(sequence.incrementAndGet());
                }
                storage.put(task.getId(), task);
                saved.add(task);
            }
            return saved;
        });

        when(repository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Optional.ofNullable(storage.get(id));
        });

        when(repository.findAll()).thenAnswer(invocation -> new ArrayList<>(storage.values()));

        when(repository.findAllById(anyIterable())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<Task> tasks = new ArrayList<>();
            for (Long id : ids) {
                Task task = storage.get(id);
                if (task != null) {
                    tasks.add(task);
                }
            }
            return tasks;
        });

        when(repository.existsById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return storage.containsKey(id);
        });

        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            storage.remove(id);
            return null;
        }).when(repository).deleteById(anyLong());

        when(repository.findAllWithAttachments()).thenAnswer(invocation -> new ArrayList<>(storage.values()));
    }
}
