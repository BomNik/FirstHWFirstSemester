package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.model.TaskAttachment;
import java.util.List;
import java.util.Optional;

public interface TaskAttachmentRepository {

    TaskAttachment create(TaskAttachment taskAttachment);

    Optional<TaskAttachment> findById(Long id);

    List<TaskAttachment> findAllAttachmentsByTaskId(Long taskId);

    TaskAttachment update(TaskAttachment taskAttachment);

    boolean deleteById(Long id);

}
