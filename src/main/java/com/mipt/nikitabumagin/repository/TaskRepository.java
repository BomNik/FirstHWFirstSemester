package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing Task entities.
 *
 * <p>This interface extends JpaRepository, providing CRUD operations and custom query methods for
 * Task entities. It allows for querying tasks based on their completion status, priority, title,
 * and due date.</p>
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCompleted(Boolean completed);

    List<Task> findByPriority(Priority priority);

    List<Task> findByCompletedAndPriority(Boolean completed, Priority priority);

    List<Task> findByTitleContainingIgnoreCase(String title);

    List<Task> findByDueDateBefore(LocalDateTime dueDate);

    List<Task> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    List<Task> findByCompletedFalse();

    List<Task> findByCompletedTrue();

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.dueDate IS NOT NULL
              AND t.dueDate BETWEEN :now AND :sevenDaysLater
            ORDER BY t.dueDate ASC
            """)
    List<Task> findTasksDueInNext7Days(@Param("now") LocalDateTime now,
            @Param("sevenDaysLater") LocalDateTime sevenDaysLater);

    @EntityGraph(attributePaths = "attachments")
    @Query("SELECT t FROM Task t")
    List<Task> findAllWithAttachments();
}
