package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.dto.TaskAttachmentResponseDto;
import com.mipt.nikitabumagin.dto.mapper.TaskAttachmentMapper;
import com.mipt.nikitabumagin.exception.AttachmentNotFoundException;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import com.mipt.nikitabumagin.model.TaskAttachment;
import com.mipt.nikitabumagin.repository.TaskAttachmentRepository;
import com.mipt.nikitabumagin.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final TaskAttachmentMapper attachmentMapper;
    private final Path uploadDir;

    public AttachmentService(TaskAttachmentRepository attachmentRepository,
            TaskAttachmentMapper attachmentMapper, TaskRepository taskRepository,
            @Value("${app.attachments.upload-dir:uploads}") String storagePath) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentMapper = attachmentMapper;
        this.taskRepository = taskRepository;
        this.uploadDir = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to initialize attachment storage directory: " + uploadDir, e);
        }
    }

    public TaskAttachmentResponseDto storeAttachment(Long taskId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file must not be empty");
        }

        taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));

        String fileName = extractOriginalFileName(file);
        String storedFileName = UUID.randomUUID().toString();
        Path targetPath = uploadDir.resolve(storedFileName);

        TaskAttachment attachment = TaskAttachment.builder()
                .taskId(taskId)
                .fileName(fileName)
                .storedFileName(storedFileName)
                .contentType(resolveContentType(file))
                .size(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            try {
                TaskAttachment saved = attachmentRepository.create(attachment);
                return attachmentMapper.toResponseDto(saved);
            } catch (RuntimeException e) {
                deleteFileQuietly(targetPath);
                throw e;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store attachment file: " + fileName, e);
        }
    }

    public TaskAttachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));
    }

    public Resource loadAsResource(Long attachmentId) {
        TaskAttachment attachment = getAttachment(attachmentId);
        Path storedFilePath = resolveStoredFilePath(attachment);

        try {
            Resource resource = new UrlResource(storedFilePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new IllegalStateException(
                    "Stored attachment file is missing or unreadable: " + storedFilePath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load attachment file: " + attachment.getFileName(), e);
        }
    }

    public void deleteAttachment(Long attachmentId) {
        TaskAttachment attachment = getAttachment(attachmentId);
        Path storedFilePath = resolveStoredFilePath(attachment);

        try {
            Files.deleteIfExists(storedFilePath);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to delete attachment file: " + attachment.getFileName(), e);
        }

        if (!attachmentRepository.deleteById(attachmentId)) {
            throw new AttachmentNotFoundException(attachmentId);
        }
    }

    public List<TaskAttachmentResponseDto> getAttachmentsByTaskId(Long taskId) {
        taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));

        return attachmentRepository.findAllAttachmentsByTaskId(taskId).stream()
                .map(attachmentMapper::toResponseDto)
                .toList();
    }

    private String extractOriginalFileName(MultipartFile file) {
        String cleanedPath = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), ""));
        String fileName = Paths.get(cleanedPath).getFileName().toString();
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("Original filename must not be blank");
        }
        return fileName;
    }

    private String resolveContentType(MultipartFile file) {
        return StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";
    }

    private void deleteFileQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup: the original repository error is more important here.
        }
    }

    private Path resolveStoredFilePath(TaskAttachment attachment) {
        Path storedFilePath = uploadDir.resolve(attachment.getStoredFileName()).normalize();
        if (!storedFilePath.startsWith(uploadDir)) {
            throw new IllegalStateException(
                    "Stored attachment path points outside upload directory: "
                            + attachment.getStoredFileName());
        }
        return storedFilePath;
    }
}
