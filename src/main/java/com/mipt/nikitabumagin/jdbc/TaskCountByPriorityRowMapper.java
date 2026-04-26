package com.mipt.nikitabumagin.jdbc;

import com.mipt.nikitabumagin.dto.TaskCountByPriorityDto;
import com.mipt.nikitabumagin.model.Priority;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * RowMapper implementation for mapping SQL query results to TaskCountByPriorityDto objects.
 *
 * <p>This class implements the RowMapper interface, providing a method to convert each row of a
 * ResultSet into a TaskCountByPriorityDto. It extracts the 'priority' and 'task_count' fields from
 * the ResultSet and constructs a TaskCountByPriorityDto using the builder pattern.</p>
 */
@Component
public class TaskCountByPriorityRowMapper implements RowMapper<TaskCountByPriorityDto> {

    @Override
    public TaskCountByPriorityDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TaskCountByPriorityDto.builder()
                .priority(Priority.valueOf(rs.getString("priority")))
                .count(rs.getLong("task_count"))
                .build();
    }
}
