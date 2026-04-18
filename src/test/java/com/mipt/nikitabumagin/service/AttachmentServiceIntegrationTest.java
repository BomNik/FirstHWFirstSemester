package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.dto.TaskAttachmentResponseDto;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.model.TaskAttachment;
import com.mipt.nikitabumagin.repository.TaskAttachmentRepository;
import com.mipt.nikitabumagin.repository.TaskRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttachmentServiceIntegrationTest {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.attachments.upload-dir", () -> tempDir.toString());
    }

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    private Task savedTask;

    @BeforeEach
    void setUp() throws IOException {
        savedTask = taskRepository.save(Task.builder()
                .title("Task for attachments")
                .description("Attachment target")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(Priority.MEDIUM)
                .tags(Set.of("attachments"))
                .build());

        Files.createDirectories(tempDir);
    }

    @Test
    @DisplayName("storeAttachment должен сохранять метаданные в БД и файл на диск")
    void storeAttachment_shouldSaveMetadataAndFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.txt",
                "text/plain",
                "hello attachment".getBytes()
        );

        TaskAttachmentResponseDto response =
                attachmentService.storeAttachment(savedTask.getId(), file);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.taskId()).isEqualTo(savedTask.getId());
        assertThat(response.fileName()).isEqualTo("report.txt");

        TaskAttachment savedAttachment =
                taskAttachmentRepository.findById(response.id()).orElseThrow();

        assertThat(savedAttachment.getTask().getId()).isEqualTo(savedTask.getId());
        assertThat(savedAttachment.getStoredFileName()).isNotBlank();

        Path storedPath = tempDir.resolve(savedAttachment.getStoredFileName());
        assertThat(Files.exists(storedPath)).isTrue();
        assertThat(Files.readString(storedPath)).isEqualTo("hello attachment");
    }

    @Test
    @DisplayName("getAttachmentsByTaskId должен возвращать вложения конкретной задачи")
    void getAttachmentsByTaskId_shouldReturnTaskAttachments() {
        taskAttachmentRepository.save(TaskAttachment.builder()
                .task(savedTask)
                .fileName("a.txt")
                .storedFileName("uuid-a")
                .contentType("text/plain")
                .size(10L)
                .uploadedAt(LocalDateTime.now())
                .build());

        taskAttachmentRepository.save(TaskAttachment.builder()
                .task(savedTask)
                .fileName("b.txt")
                .storedFileName("uuid-b")
                .contentType("text/plain")
                .size(20L)
                .uploadedAt(LocalDateTime.now())
                .build());

        List<TaskAttachmentResponseDto> result =
                attachmentService.getAttachmentsByTaskId(savedTask.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TaskAttachmentResponseDto::taskId)
                .containsOnly(savedTask.getId());
    }

    @Test
    @DisplayName("loadAsResource должен возвращать сохраненный файл")
    void loadAsResource_shouldReturnStoredFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "resource content".getBytes()
        );

        TaskAttachmentResponseDto response =
                attachmentService.storeAttachment(savedTask.getId(), file);

        Resource resource = attachmentService.loadAsResource(response.id());

        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
        assertThat(resource.getFilename()).isNotBlank();
        assertThat(new String(resource.getInputStream().readAllBytes()))
                .isEqualTo("resource content");
    }

    @Test
    @DisplayName("deleteAttachment должен удалять и запись в БД, и файл с диска")
    void deleteAttachment_shouldDeleteDbRecordAndPhysicalFile() {
        TaskAttachment attachment = taskAttachmentRepository.save(TaskAttachment.builder()
                .task(savedTask)
                .fileName("delete-me.txt")
                .storedFileName("stored-delete-me.txt")
                .contentType("text/plain")
                .size(15L)
                .uploadedAt(LocalDateTime.now())
                .build());

        Path storedPath = tempDir.resolve(attachment.getStoredFileName());
        try {
            Files.writeString(storedPath, "to be deleted");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThat(taskAttachmentRepository.findById(attachment.getId())).isPresent();
        assertThat(Files.exists(storedPath)).isTrue();

        attachmentService.deleteAttachment(attachment.getId());

        assertThat(taskAttachmentRepository.findById(attachment.getId())).isEmpty();
        assertThat(Files.exists(storedPath)).isFalse();
    }
}
