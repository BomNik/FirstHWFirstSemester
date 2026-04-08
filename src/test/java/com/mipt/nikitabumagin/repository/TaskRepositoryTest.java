package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.model.TaskAttachment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Test
    @DisplayName("findTasksDueInNext7Days должен возвращать только задачи с dueDate в ближайшие 7 дней")
    void findTasksDueInNext7Days_shouldReturnOnlyTasksInRange() {
        LocalDateTime now = LocalDateTime.now();

        Task taskInRange1 = Task.builder()
                .title("Task 1")
                .description("In range")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(2))
                .priority(Priority.HIGH)
                .tags(Set.of("urgent"))
                .build();

        Task taskInRange2 = Task.builder()
                .title("Task 2")
                .description("In range")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(7))
                .priority(Priority.MEDIUM)
                .tags(Set.of("week"))
                .build();

        Task taskOutOfRange = Task.builder()
                .title("Task 3")
                .description("Out of range")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(10))
                .priority(Priority.LOW)
                .tags(Set.of("later"))
                .build();

        Task taskWithoutDueDate = Task.builder()
                .title("Task 4")
                .description("No due date")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(null)
                .priority(Priority.LOW)
                .tags(Set.of("optional"))
                .build();

        taskRepository.saveAll(List.of(taskInRange1, taskInRange2, taskOutOfRange, taskWithoutDueDate));

        List<Task> result = taskRepository.findTasksDueInNext7Days(now, now.plusDays(7));

        assertThat(result)
                .extracting(Task::getTitle)
                .containsExactly("Task 1", "Task 2");

        assertThat(result)
                .allMatch(task -> task.getDueDate() != null
                        && !task.getDueDate().isBefore(now)
                        && !task.getDueDate().isAfter(now.plusDays(7)));
    }

    @Test
    @DisplayName("findByCompletedAndPriority должен корректно фильтровать задачи")
    void findByCompletedAndPriority_shouldFilterCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        Task matchingTask = Task.builder()
                .title("Matching")
                .description("Target")
                .completed(true)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(1))
                .priority(Priority.HIGH)
                .tags(Set.of("a"))
                .build();

        Task wrongCompleted = Task.builder()
                .title("Wrong completed")
                .description("No")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(1))
                .priority(Priority.HIGH)
                .tags(Set.of("b"))
                .build();

        Task wrongPriority = Task.builder()
                .title("Wrong priority")
                .description("No")
                .completed(true)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(1))
                .priority(Priority.LOW)
                .tags(Set.of("c"))
                .build();

        taskRepository.saveAll(List.of(matchingTask, wrongCompleted, wrongPriority));

        List<Task> result = taskRepository.findByCompletedAndPriority(true, Priority.HIGH);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Matching");
    }

    @Test
    @DisplayName("должно сохраняться отношение задача-вложение")
    void shouldSaveTaskWithAttachmentRelationship() {
        LocalDateTime now = LocalDateTime.now();

        Task task = Task.builder()
                .title("Task with attachment")
                .description("Check relation")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(3))
                .priority(Priority.MEDIUM)
                .tags(Set.of("files"))
                .build();

        Task savedTask = taskRepository.save(task);

        TaskAttachment attachment = TaskAttachment.builder()
                .task(savedTask)
                .fileName("report.pdf")
                .storedFileName("uuid-report.pdf")
                .contentType("application/pdf")
                .size(1024L)
                .uploadedAt(now)
                .build();

        TaskAttachment savedAttachment = taskAttachmentRepository.save(attachment);

        List<TaskAttachment> attachments = taskAttachmentRepository.findByTask_Id(savedTask.getId());

        assertThat(savedAttachment.getId()).isNotNull();
        assertThat(attachments).hasSize(1);
        assertThat(attachments.getFirst().getTask().getId()).isEqualTo(savedTask.getId());
        assertThat(attachments.getFirst().getFileName()).isEqualTo("report.pdf");
    }
}
