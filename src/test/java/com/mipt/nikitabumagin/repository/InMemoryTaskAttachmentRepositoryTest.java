package com.mipt.nikitabumagin.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mipt.nikitabumagin.exception.AttachmentNotFoundException;
import com.mipt.nikitabumagin.model.TaskAttachment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryTaskAttachmentRepositoryTest {

    private InMemoryTaskAttachmentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskAttachmentRepository();
    }

    @Test
    void create_assignsIdAndStoresAttachment() {
        TaskAttachment attachment = attachment(null, 10L, "notes.pdf");

        TaskAttachment created = repository.create(attachment);

        assertEquals(1L, created.getId());
        assertSame(created, repository.findById(created.getId()).orElseThrow());
    }

    @Test
    void findAllAttachmentsByTaskId_returnsOnlyMatchingAttachments() {
        TaskAttachment task10First = repository.create(attachment(null, 10L, "first.txt"));
        TaskAttachment task10Second = repository.create(attachment(null, 10L, "second.txt"));
        repository.create(attachment(null, 20L, "third.txt"));

        List<TaskAttachment> attachments = repository.findAllAttachmentsByTaskId(10L);

        assertEquals(2, attachments.size());
        assertEquals(Set.of(task10First.getId(), task10Second.getId()),
                attachments.stream().map(TaskAttachment::getId).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void update_replacesStoredAttachmentWhenIdExists() {
        TaskAttachment created = repository.create(attachment(null, 10L, "draft.txt"));
        TaskAttachment updated = attachment(created.getId(), 10L, "final.txt");

        TaskAttachment result = repository.update(updated);

        assertSame(updated, result);
        assertEquals("final.txt", repository.findById(created.getId()).orElseThrow().getFileName());
    }

    @Test
    void update_throwsWhenAttachmentDoesNotExist() {
        TaskAttachment missing = attachment(999L, 10L, "missing.txt");

        assertThrows(AttachmentNotFoundException.class, () -> repository.update(missing));
    }

    @Test
    void deleteById_removesStoredAttachment() {
        TaskAttachment created = repository.create(attachment(null, 10L, "notes.pdf"));

        assertTrue(repository.deleteById(created.getId()));
        assertFalse(repository.findById(created.getId()).isPresent());
    }

    private TaskAttachment attachment(Long id, Long taskId, String fileName) {
        return TaskAttachment.builder()
                .id(id)
                .taskId(taskId)
                .fileName(fileName)
                .storedFileName("stored-" + fileName)
                .contentType("text/plain")
                .size(123L)
                .uploadedAt(LocalDateTime.of(2026, 3, 21, 12, 0))
                .build();
    }
}
