package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.dto.TaskAttachmentCreateDto;
import com.mipt.nikitabumagin.dto.TaskAttachmentUpdateDto;
import com.mipt.nikitabumagin.model.TaskAttachment;
import java.util.List;
import java.util.Optional;

public interface TaskAttachmentRepository {

    TaskAttachment create(TaskAttachmentCreateDto request);

    Optional<TaskAttachment> findById(Long id);

    List<TaskAttachment> findAllAttachmentsByTaskId(Long taskId);

    TaskAttachment update(Long id, TaskAttachmentUpdateDto request);

    boolean deleteById(Long id);

}
