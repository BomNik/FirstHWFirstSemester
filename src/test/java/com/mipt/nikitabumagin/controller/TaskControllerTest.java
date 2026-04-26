package com.mipt.nikitabumagin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.security.JwtAuthFilter;
import com.mipt.nikitabumagin.service.TaskService;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createTask_returnsCreatedStatusAndJsonBody() throws Exception {
        TaskCreateDto request = new TaskCreateDto(
                "Test task",
                "Task description",
                LocalDateTime.now().plusDays(2).withNano(0),
                Priority.HIGH,
                Set.of("study", "spring")
        );

        TaskResponseDto created = new TaskResponseDto(
                1L,
                request.getTitle(),
                request.getDescription(),
                false,
                LocalDateTime.now().withNano(0),
                request.getDueDate(),
                request.getPriority(),
                request.getTags()
        );

        when(taskService.createTask(any(TaskCreateDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.description").value("Task description"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void getTaskById_returnsExistingTaskAndJsonBody() throws Exception {
        Long taskId = 10L;
        TaskResponseDto existing = new TaskResponseDto(
                taskId,
                "Existing task",
                "Stored task",
                false,
                LocalDateTime.now().minusDays(1).withNano(0),
                LocalDateTime.now().plusDays(3).withNano(0),
                Priority.MEDIUM,
                Set.of("api")
        );

        when(taskService.getTaskById(taskId)).thenReturn(existing);

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Existing task"))
                .andExpect(jsonPath("$.description").value("Stored task"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void createTask_invalidRequest_returnsBadRequestAndDoesNotCallService() throws Exception {
        TaskCreateDto invalid = new TaskCreateDto(
                "",
                "Description",
                LocalDateTime.now().plusDays(1).withNano(0),
                Priority.MEDIUM,
                Set.of("validation")
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(TaskCreateDto.class));
    }
}
