package com.mipt.nikitabumagin.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mipt.nikitabumagin.dto.TaskAttachmentResponseDto;
import com.mipt.nikitabumagin.dto.mapper.TaskAttachmentMapper;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.model.TaskAttachment;
import com.mipt.nikitabumagin.repository.TaskAttachmentRepository;
import com.mipt.nikitabumagin.repository.TaskRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

class AttachmentServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void initCreatesDirectoryAndStoreAttachmentSavesFileAndMetadata() throws Exception {
        TestTaskAttachmentRepository attachmentRepository = new TestTaskAttachmentRepository();
        TestTaskRepository taskRepository = new TestTaskRepository();
        taskRepository.create(Task.builder()
                .title("Task with file")
                .description("Description")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.MEDIUM)
                .tags(Set.of("files"))
                .build());

        TaskAttachmentMapper mapper = Mappers.getMapper(TaskAttachmentMapper.class);
        Path uploadDir = tempDir.resolve("uploads");

        AttachmentService service = new AttachmentService(
                attachmentRepository,
                mapper,
                taskRepository,
                uploadDir.toString());

        service.init();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello attachment".getBytes(UTF_8));

        TaskAttachmentResponseDto response = service.storeAttachment(1L, file);

        assertTrue(Files.isDirectory(uploadDir));
        assertEquals("notes.txt", response.fileName());
        assertEquals("text/plain", response.contentType());
        assertEquals(file.getSize(), response.size());

        TaskAttachment saved = attachmentRepository.findById(response.id()).orElseThrow();
        Path storedFile = uploadDir.resolve(saved.getStoredFileName());
        assertTrue(Files.exists(storedFile));
        assertEquals("hello attachment", Files.readString(storedFile, UTF_8));
    }

    @Test
    void getAndListAttachments_returnStoredMetadata() {
        AttachmentService service = createServiceWithTask();

        TaskAttachmentResponseDto first = service.storeAttachment(
                1L,
                new MockMultipartFile("file", "notes.txt", "text/plain", "one".getBytes(UTF_8)));
        TaskAttachmentResponseDto second = service.storeAttachment(
                1L,
                new MockMultipartFile("file", "report.txt", "text/plain", "two".getBytes(UTF_8)));

        TaskAttachment attachment = service.getAttachment(first.id());
        List<TaskAttachmentResponseDto> attachments = service.getAttachmentsByTaskId(1L);

        assertEquals(first.id(), attachment.getId());
        assertEquals("notes.txt", attachment.getFileName());
        assertEquals(2, attachments.size());
        assertEquals(Set.of(first.id(), second.id()),
                attachments.stream().map(TaskAttachmentResponseDto::id).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void loadAsResource_returnsStoredFileContents() throws Exception {
        AttachmentService service = createServiceWithTask();
        TaskAttachmentResponseDto response = service.storeAttachment(
                1L,
                new MockMultipartFile("file", "guide.txt", "text/plain", "download me".getBytes(UTF_8)));

        Resource resource = service.loadAsResource(response.id());

        assertTrue(resource.exists());
        assertEquals("download me", Files.readString(resource.getFile().toPath(), UTF_8));
    }

    @Test
    void deleteAttachment_removesFileAndMetadata() throws Exception {
        TestTaskAttachmentRepository attachmentRepository = new TestTaskAttachmentRepository();
        TestTaskRepository taskRepository = new TestTaskRepository();
        taskRepository.create(Task.builder()
                .title("Task with file")
                .description("Description")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.MEDIUM)
                .tags(Set.of("files"))
                .build());

        TaskAttachmentMapper mapper = Mappers.getMapper(TaskAttachmentMapper.class);
        Path uploadDir = tempDir.resolve("uploads-delete");
        AttachmentService service = new AttachmentService(
                attachmentRepository,
                mapper,
                taskRepository,
                uploadDir.toString());
        service.init();

        TaskAttachmentResponseDto response = service.storeAttachment(
                1L,
                new MockMultipartFile("file", "trash.txt", "text/plain", "remove me".getBytes(UTF_8)));

        TaskAttachment saved = attachmentRepository.findById(response.id()).orElseThrow();
        Path storedFile = uploadDir.resolve(saved.getStoredFileName());
        assertTrue(Files.exists(storedFile));

        service.deleteAttachment(response.id());

        assertFalse(Files.exists(storedFile));
        assertTrue(attachmentRepository.findById(response.id()).isEmpty());
    }

    private AttachmentService createServiceWithTask() {
        TestTaskAttachmentRepository attachmentRepository = new TestTaskAttachmentRepository();
        TestTaskRepository taskRepository = new TestTaskRepository();
        taskRepository.create(Task.builder()
                .title("Task with file")
                .description("Description")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.MEDIUM)
                .tags(Set.of("files"))
                .build());

        TaskAttachmentMapper mapper = Mappers.getMapper(TaskAttachmentMapper.class);
        Path uploadDir = tempDir.resolve("uploads-shared");
        AttachmentService service = new AttachmentService(
                attachmentRepository,
                mapper,
                taskRepository,
                uploadDir.toString());
        service.init();
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

    private static class TestTaskAttachmentRepository implements TaskAttachmentRepository {

        private final Map<Long, TaskAttachment> storage = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public TaskAttachment create(TaskAttachment taskAttachment) {
            long id = sequence.incrementAndGet();
            taskAttachment.setId(id);
            storage.put(id, taskAttachment);
            return taskAttachment;
        }

        @Override
        public Optional<TaskAttachment> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<TaskAttachment> findAllAttachmentsByTaskId(Long taskId) {
            return storage.values().stream()
                    .filter(attachment -> attachment.getTaskId().equals(taskId))
                    .toList();
        }

        @Override
        public TaskAttachment update(TaskAttachment taskAttachment) {
            storage.put(taskAttachment.getId(), taskAttachment);
            return taskAttachment;
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }
}
