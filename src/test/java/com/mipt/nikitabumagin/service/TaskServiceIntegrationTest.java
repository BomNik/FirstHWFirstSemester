package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.exception.TaskBulkOperationException;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.model.TaskAttachment;
import com.mipt.nikitabumagin.repository.TaskAttachmentRepository;
import com.mipt.nikitabumagin.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Test
    @DisplayName("createTask должен сохранять задачу в БД")
    void createTask_shouldPersistTask() {
        TaskCreateDto dto = new TaskCreateDto(
                "New task",
                "Task description",
                LocalDateTime.now().plusDays(2),
                Priority.HIGH,
                Set.of("study", "spring")
        );

        TaskResponseDto response = taskService.createTask(dto);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("New task");
        assertThat(response.priority()).isEqualTo(Priority.HIGH);

        Task savedTask = taskRepository.findById(response.id()).orElseThrow();
        assertThat(savedTask.getTitle()).isEqualTo("New task");
        assertThat(savedTask.getCompleted()).isFalse();
    }

    @Test
    @DisplayName("getTaskById должен возвращать сохраненную задачу")
    void getTaskById_shouldReturnTask() {
        Task task = taskRepository.save(Task.builder()
                .title("Existing")
                .description("Stored task")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.MEDIUM)
                .tags(Set.of("existing"))
                .build());

        TaskResponseDto response = taskService.getTaskById(task.getId());

        assertThat(response.id()).isEqualTo(task.getId());
        assertThat(response.title()).isEqualTo("Existing");
    }

    @Test
    @DisplayName("updateTask должен изменять поля существующей задачи")
    void updateTask_shouldModifyExistingTask() {
        Task task = taskRepository.save(Task.builder()
                .title("Old title")
                .description("Old description")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(Priority.LOW)
                .tags(new HashSet<>(List.of("old")))
                .build());

        TaskUpdateDto dto = new TaskUpdateDto(
                "Updated title",
                "Updated description",
                true,
                LocalDateTime.now().plusDays(5),
                Priority.HIGH,
                Set.of("updated", "important")
        );

        TaskResponseDto response = taskService.updateTask(task.getId(), dto);

        assertThat(response.title()).isEqualTo("Updated title");
        assertThat(response.completed()).isTrue();
        assertThat(response.priority()).isEqualTo(Priority.HIGH);

        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getTitle()).isEqualTo("Updated title");
        assertThat(updatedTask.getCompleted()).isTrue();
        assertThat(updatedTask.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("bulkCompleteTasks должен помечать все указанные задачи как выполненные")
    void bulkCompleteTasks_shouldMarkTasksCompleted() {
        Task task1 = taskRepository.save(Task.builder()
                .title("Task 1")
                .description("Desc 1")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.LOW)
                .tags(Set.of("a"))
                .build());

        Task task2 = taskRepository.save(Task.builder()
                .title("Task 2")
                .description("Desc 2")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(Priority.MEDIUM)
                .tags(Set.of("b"))
                .build());

        taskService.bulkCompleteTasks(List.of(task1.getId(), task2.getId()));

        Task updated1 = taskRepository.findById(task1.getId()).orElseThrow();
        Task updated2 = taskRepository.findById(task2.getId()).orElseThrow();

        assertThat(updated1.getCompleted()).isTrue();
        assertThat(updated2.getCompleted()).isTrue();
    }

    @Test
    @DisplayName("bulkCompleteTasks должен откатывать транзакцию, если один из id не существует")
    void bulkCompleteTasks_shouldRollbackWhenAnyIdDoesNotExist() {
        Task task1 = taskRepository.save(Task.builder()
                .title("Task 1")
                .description("Desc 1")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.LOW)
                .tags(Set.of("a"))
                .build());

        Task task2 = taskRepository.save(Task.builder()
                .title("Task 2")
                .description("Desc 2")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(Priority.MEDIUM)
                .tags(Set.of("b"))
                .build());

        assertThatThrownBy(() ->
                taskService.bulkCompleteTasks(List.of(task1.getId(), task2.getId(), 999999L))
        ).isInstanceOf(TaskBulkOperationException.class);

        Task unchanged1 = taskRepository.findById(task1.getId()).orElseThrow();
        Task unchanged2 = taskRepository.findById(task2.getId()).orElseThrow();

        assertThat(unchanged1.getCompleted()).isFalse();
        assertThat(unchanged2.getCompleted()).isFalse();
    }

    @Test
    @DisplayName("getAllTasksWithAttachments должен возвращать задачи вместе с вложениями")
    void getAllTasksWithAttachments_shouldReturnTasksWithAttachments() {
        Task task = taskRepository.save(Task.builder()
                .title("Task with files")
                .description("Has attachments")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(3))
                .priority(Priority.HIGH)
                .tags(Set.of("files"))
                .build());

        taskAttachmentRepository.save(TaskAttachment.builder()
                .task(task)
                .fileName("file1.txt")
                .storedFileName("uuid-1")
                .contentType("text/plain")
                .size(10L)
                .uploadedAt(LocalDateTime.now())
                .build());

        taskAttachmentRepository.save(TaskAttachment.builder()
                .task(task)
                .fileName("file2.txt")
                .storedFileName("uuid-2")
                .contentType("text/plain")
                .size(20L)
                .uploadedAt(LocalDateTime.now())
                .build());

        List<TaskResponseDto> result = taskService.getAllTasksWithAttachments();

        assertThat(result).isNotEmpty();
        assertThat(result)
                .extracting(TaskResponseDto::id)
                .contains(task.getId());
    }
}
