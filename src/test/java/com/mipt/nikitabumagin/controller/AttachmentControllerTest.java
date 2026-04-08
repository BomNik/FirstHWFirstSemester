package com.mipt.nikitabumagin.controller;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mipt.nikitabumagin.dto.mapper.TaskAttachmentMapper;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.model.TaskAttachment;
import com.mipt.nikitabumagin.repository.TaskAttachmentRepository;
import com.mipt.nikitabumagin.repository.TaskRepository;
import com.mipt.nikitabumagin.service.AttachmentService;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.hamcrest.Matchers;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AttachmentControllerTest {

    @TempDir
    Path tempDir;

    private MockMvc mockMvc;
    private TaskAttachmentRepository attachmentRepository;
    private Map<Long, TaskAttachment> attachmentStorage;
    private AtomicLong attachmentSequence;

    @BeforeEach
    void setUp() {
        attachmentStorage = new LinkedHashMap<>();
        attachmentSequence = new AtomicLong();

        attachmentRepository = mock(TaskAttachmentRepository.class);
        TaskRepository taskRepository = mock(TaskRepository.class);

        Task seedTask = Task.builder()
                .id(1L)
                .title("Task with attachment")
                .description("Description")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.MEDIUM)
                .tags(Set.of("files"))
                .build();

        when(taskRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return id.equals(seedTask.getId()) ? Optional.of(seedTask) : Optional.empty();
        });

        when(attachmentRepository.save(any(TaskAttachment.class))).thenAnswer(invocation -> {
            TaskAttachment attachment = invocation.getArgument(0);
            if (attachment.getId() == null) {
                attachment.setId(attachmentSequence.incrementAndGet());
            }
            attachmentStorage.put(attachment.getId(), attachment);
            return attachment;
        });

        when(attachmentRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Optional.ofNullable(attachmentStorage.get(id));
        });

        when(attachmentRepository.findByTask_Id(anyLong())).thenAnswer(invocation -> {
            Long taskId = invocation.getArgument(0);
            return attachmentStorage.values().stream()
                    .filter(attachment -> attachment.getTask() != null)
                    .filter(attachment -> taskId.equals(attachment.getTask().getId()))
                    .collect(Collectors.toList());
        });

        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            attachmentStorage.remove(id);
            return null;
        }).when(attachmentRepository).deleteById(anyLong());

        AttachmentService attachmentService = new AttachmentService(
                attachmentRepository,
                Mappers.getMapper(TaskAttachmentMapper.class),
                taskRepository,
                tempDir.resolve("uploads").toString());
        attachmentService.init();

        AttachmentController controller = new AttachmentController(attachmentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void uploadAttachment_returnsCreatedMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes(UTF_8));

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", 1L).file(file))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/attachments/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.fileName").value("notes.txt"))
                .andExpect(jsonPath("$.contentType").value("text/plain"))
                .andExpect(jsonPath("$.size").value(file.getSize()));
    }

    @Test
    void getAttachmentsByTaskId_returnsListOfMetadata() throws Exception {
        attachmentRepository.save(attachment(1L, "first.txt", "stored-1", 3L));
        attachmentRepository.save(attachment(1L, "second.txt", "stored-2", 4L));
        attachmentRepository.save(attachment(2L, "other.txt", "stored-3", 5L));

        mockMvc.perform(get("/api/tasks/{taskId}/attachments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].taskId").value(1))
                .andExpect(jsonPath("$[1].taskId").value(1));
    }

    @Test
    void downloadAttachment_returnsFileWithHeaders() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "guide.txt",
                "text/plain",
                "download me".getBytes(UTF_8));

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", 1L).file(file))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/attachments/{attachmentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(header().string("Content-Disposition",
                        Matchers.containsString("filename*=UTF-8''guide.txt")))
                .andExpect(content().bytes("download me".getBytes(UTF_8)));
    }

    @Test
    void deleteAttachment_returnsNoContentAndRemovesMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "trash.txt",
                "text/plain",
                "remove".getBytes(UTF_8));

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", 1L).file(file))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/attachments/{attachmentId}", 1L))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/attachments/{attachmentId}", 1L))
                .andExpect(status().isNotFound());
    }

    private TaskAttachment attachment(Long taskId, String fileName, String storedFileName,
            Long size) {
        return TaskAttachment.builder()
                .task(Task.builder().id(taskId).build())
                .fileName(fileName)
                .storedFileName(storedFileName)
                .contentType("text/plain")
                .size(size)
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
