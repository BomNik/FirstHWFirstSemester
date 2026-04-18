package com.mipt.nikitabumagin.dto;

import com.mipt.nikitabumagin.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCountByPriorityDto {

    private Priority priority;
    private Long count;
}
