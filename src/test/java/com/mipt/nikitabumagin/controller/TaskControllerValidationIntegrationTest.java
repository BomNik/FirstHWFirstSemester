package com.mipt.nikitabumagin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.dto.mapper.TaskMapper;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.repository.TaskRepository;
import com.mipt.nikitabumagin.service.TaskService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class TaskControllerValidationIntegrationTest {

    private MockMvc mockMvc;
    private TaskRepository taskRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        LocalValidatorFactoryBean springValidator = new LocalValidatorFactoryBean();
        springValidator.afterPropertiesSet();

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);
        taskRepository = new TestTaskRepository();

        TaskService taskService = new TaskService(taskRepository, taskMapper, validator);
        taskService.initCache();

        TaskController controller = new TaskController(taskService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(springValidator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void updateTask_dueDateBeforeCreation_returnsBadRequestAndKeepsStoredTaskUnchanged()
            throws Exception {
        LocalDateTime createdAt = LocalDateTime.now().plusDays(2).withNano(0);
        LocalDateTime initialDueDate = createdAt.plusDays(2);

        Task storedTask = new Task();
        storedTask.setTitle("Validation case");
        storedTask.setDescription("Created directly in repository");
        storedTask.setCompleted(false);
        storedTask.setCreatedAt(createdAt);
        storedTask.setDueDate(initialDueDate);
        storedTask.setPriority(Priority.MEDIUM);
        storedTask.setTags(Set.of("validation"));
        Task created = taskRepository.create(storedTask);

        TaskUpdateDto invalidUpdate = new TaskUpdateDto(
                null,
                null,
                null,
                createdAt.minusDays(1),
                null,
                null);

        mockMvc.perform(patch("/api/tasks/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());

        Task actual = taskRepository.findById(created.getId()).orElseThrow();
        assertEquals(initialDueDate, actual.getDueDate());
        assertEquals(createdAt, actual.getCreatedAt());
    }

    private static class TestTaskRepository implements TaskRepository {

        private final Map<Long, Task> storage = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Task create(Task task) {
            long id = sequence.incrementAndGet();
            task.setId(id);
            storage.put(id, task);
            return task;
        }

        @Override
        public Optional<Task> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Task> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public Task update(Task task) {
            storage.put(task.getId(), task);
            return task;
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }
}
