package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.exception.AttachmentNotFoundException;
import com.mipt.nikitabumagin.model.TaskAttachment;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * Primary {@link TaskAttachmentRepository} implementation that stores attachment metadata in memory
 * using a {@link java.util.concurrent.ConcurrentHashMap}.
 *
 * <p>Thread-safe and suitable for the MVP stage where persistent storage is not
 * required. An {@link java.util.concurrent.atomic.AtomicLong} sequence generator ensures unique
 * attachment identifiers.</p>
 */
@Primary
@Repository
public class InMemoryTaskAttachmentRepository implements TaskAttachmentRepository {

    private final Map<Long, TaskAttachment> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    InMemoryTaskAttachmentRepository() {
    }

    @Override
    public TaskAttachment create(TaskAttachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment must not be null");
        }
        Long id = idSequence.incrementAndGet();
        attachment.setId(id);
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
        if (taskId == null) {
            return List.of();
        }
        return storage.values().stream()
                .filter(attachment -> Objects.equals(taskId, attachment.getTaskId()))
                .toList();
    }

    @Override
    public TaskAttachment update(TaskAttachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment must not be null");
        }
        if (attachment.getId() == null
                || !storage.containsKey(attachment.getId())) {
            throw new AttachmentNotFoundException(attachment.getId());
        }
        storage.put(attachment.getId(), attachment);
        return attachment;
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }
}
