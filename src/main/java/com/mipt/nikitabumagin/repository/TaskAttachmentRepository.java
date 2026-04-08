package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.model.TaskAttachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing TaskAttachment entities.
 *
 * <p>This interface extends JpaRepository, providing CRUD operations and custom query methods for
 * TaskAttachment entities. It allows for querying attachments based on their associated task ID and
 * file name, as well as deleting attachments by task ID.</p>
 */
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    List<TaskAttachment> findByTask_Id(Long taskId);

    List<TaskAttachment> findByFileNameContainingIgnoreCase(String fileName);

    void deleteByTask_Id(Long taskId);
}
