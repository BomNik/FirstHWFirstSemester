package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.dto.TaskAttachmentCreateDto;
import com.mipt.nikitabumagin.dto.TaskAttachmentUpdateDto;
import com.mipt.nikitabumagin.dto.mapper.TaskAttachmentMapper;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import com.mipt.nikitabumagin.model.TaskAttachment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class InMemoryTaskAttachmentRepository implements TaskAttachmentRepository {

    private final AtomicLong idSequence = new AtomicLong(0);
    private final Map<Long, TaskAttachment> storage = new ConcurrentHashMap<>();
    private final TaskAttachmentMapper attachmentMapper;

    InMemoryTaskAttachmentRepository(TaskAttachmentMapper attachmentMapper) {
        this.attachmentMapper = attachmentMapper;
    }

    @Override
    public TaskAttachment create(TaskAttachmentCreateDto request) {
        Long id = idSequence.incrementAndGet();
        TaskAttachment attachment = new TaskAttachment();
        attachment.setId(id);
        attachment.setUploadedAt(LocalDateTime.now());
        attachment.setStoredFileName(UUID.randomUUID().toString());
        storage.put(id, attachment);
        return attachment;
    }

    @Override
    public Optional<TaskAttachment> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<TaskAttachment> findAllAttachmentsByTaskId(Long taskId) {
        return new ArrayList<>(storage.values());
    }

    @Override
    public TaskAttachment update(Long id, TaskAttachmentUpdateDto request) {
        if (id == null || !storage.containsKey(id)) {
            throw new TaskNotFoundException(id);
        }

        TaskAttachment attachmentToUpdate = storage.get(id);
        TaskAttachment updated = attachmentMapper.updateEntity(request, attachmentToUpdate);
        storage.put(id, updated);

        return updated;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }
}
