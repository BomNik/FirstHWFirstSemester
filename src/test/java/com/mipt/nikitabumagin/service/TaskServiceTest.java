package com.mipt.nikitabumagin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private SecurityFilterChain securityFilterChain;

    @BeforeEach
    void setUp() {
        reset(taskRepository);
    }

    @Test
    void updateTask_existingTask_updatesCompletedStatus_andVerifiesRepositoryInteraction() {
        Long taskId = 101L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        Task existingTask = Task.builder()
                .id(taskId)
                .title("Initial title")
                .description("Initial description")
                .completed(false)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .dueDate(createdAt.plusDays(2))
                .priority(Priority.MEDIUM)
                .tags(Set.of("study"))
                .build();

        TaskUpdateDto updateDto = new TaskUpdateDto(
                null,
                null,
                true,
                null,
                null,
                null
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        // when
        TaskResponseDto result = taskService.updateTask(taskId, updateDto);

        // then
        assertThat(result.completed()).isTrue();
        assertThat(result.id()).isEqualTo(taskId);

        ArgumentCaptor<Task> savedTaskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(savedTaskCaptor.capture());
        verifyNoMoreInteractions(taskRepository);

        Task savedTask = savedTaskCaptor.getValue();
        assertThat(savedTask.getCompleted()).isTrue();
        assertThat(savedTask.getId()).isEqualTo(taskId);
    }
}
