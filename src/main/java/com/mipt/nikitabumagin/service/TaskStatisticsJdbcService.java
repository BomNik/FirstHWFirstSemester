package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.dto.TaskCountByPriorityDto;
import com.mipt.nikitabumagin.jdbc.TaskCountByPriorityRowMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Service class for retrieving task statistics using JDBC.
 *
 * <p>This service provides methods to execute raw SQL queries to gather statistics about tasks, such
 * as counting the number of tasks by their priority. It uses JdbcTemplate for database interactions
 * and a custom RowMapper to map query results to DTOs.</p>
 */
@Service
@RequiredArgsConstructor
public class TaskStatisticsJdbcService {

    private final JdbcTemplate jdbcTemplate;
    private final TaskCountByPriorityRowMapper taskCountByPriorityRowMapper;

    public List<TaskCountByPriorityDto> getTasksCountByPriority() {
        String sql = """
                SELECT priority, COUNT(*) AS task_count
                FROM tasks
                GROUP BY priority
                ORDER BY priority
                """;

        return jdbcTemplate.query(sql, taskCountByPriorityRowMapper);
    }
}
