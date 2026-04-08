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
class TaskAttachmentRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Test
    @DisplayName("findByTask_Id должен возвращать вложения только нужной задачи")
    void findByTaskId_shouldReturnOnlyTaskAttachments() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = taskRepository.save(Task.builder()
                .title("Task 1")
                .description("First")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(1))
                .priority(Priority.HIGH)
                .tags(Set.of("t1"))
                .build());

        Task task2 = taskRepository.save(Task.builder()
                .title("Task 2")
                .description("Second")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(2))
                .priority(Priority.LOW)
                .tags(Set.of("t2"))
                .build());

        taskAttachmentRepository.save(TaskAttachment.builder()
                .task(task1)
                .fileName("a.txt")
                .storedFileName("uuid-a.txt")
                .contentType("text/plain")
                .size(10L)
                .uploadedAt(now)
                .build());

        taskAttachmentRepository.save(TaskAttachment.builder()
                .task(task1)
                .fileName("b.txt")
                .storedFileName("uuid-b.txt")
                .contentType("text/plain")
                .size(20L)
                .uploadedAt(now)
                .build());

        taskAttachmentRepository.save(TaskAttachment.builder()
                .task(task2)
                .fileName("c.txt")
                .storedFileName("uuid-c.txt")
                .contentType("text/plain")
                .size(30L)
                .uploadedAt(now)
                .build());

        List<TaskAttachment> result = taskAttachmentRepository.findByTask_Id(task1.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(att -> att.getTask().getId())
                .containsOnly(task1.getId());
    }

    @Test
    @DisplayName("findByFileNameContainingIgnoreCase должен искать по части имени без учета регистра")
    void findByFileNameContainingIgnoreCase_shouldSearchIgnoringCase() {
        LocalDateTime now = LocalDateTime.now();

        Task task = taskRepository.save(Task.builder()
                .title("Task")
                .description("Files")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(1))
                .priority(Priority.MEDIUM)
                .tags(Set.of("files"))
                .build());

        taskAttachmentRepository.saveAll(List.of(
                TaskAttachment.builder()
                        .task(task)
                        .fileName("Report.pdf")
                        .storedFileName("uuid-1")
                        .contentType("application/pdf")
                        .size(100L)
                        .uploadedAt(now)
                        .build(),
                TaskAttachment.builder()
                        .task(task)
                        .fileName("final_report.docx")
                        .storedFileName("uuid-2")
                        .contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        .size(200L)
                        .uploadedAt(now)
                        .build(),
                TaskAttachment.builder()
                        .task(task)
                        .fileName("image.png")
                        .storedFileName("uuid-3")
                        .contentType("image/png")
                        .size(300L)
                        .uploadedAt(now)
                        .build()
        ));

        List<TaskAttachment> result =
                taskAttachmentRepository.findByFileNameContainingIgnoreCase("report");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TaskAttachment::getFileName)
                .containsExactlyInAnyOrder("Report.pdf", "final_report.docx");
    }
}
