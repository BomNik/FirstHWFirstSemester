package com.mipt.nikitabumagin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.model.Priority;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Integration tests for {@link com.mipt.nikitabumagin.controller.TaskController}.
 *
 * <p>Uses {@link TestRestTemplate} to perform real HTTP calls against the running application
 * context and verifies the current DTO-based contract.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createTask_returnsCreatedStatusAndResponseDto() {
        TaskCreateDto request = createRequest("Test task", "Some description");

        ResponseEntity<TaskResponseDto> response = restTemplate.postForEntity(
                "/api/tasks", request, TaskResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        TaskResponseDto body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.id());
        assertEquals("Test task", body.title());
        assertEquals("Some description", body.description());
        assertFalse(body.completed());
        assertEquals(request.getDueDate(), body.dueDate());
        assertEquals(request.getPriority(), body.priority());
        assertEquals(request.getTags(), body.tags());
        assertTrue(response.getHeaders().containsKey("Location"));
    }

    @Test
    void createTask_blankTitle_returnsBadRequest() {
        TaskCreateDto request = createRequest("", "description");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/tasks", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createTask_nullTitle_returnsBadRequest() {
        TaskCreateDto request = createRequest(null, "description");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/tasks", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createTask_missingPriority_returnsBadRequest() {
        TaskCreateDto request = new TaskCreateDto(
                "Priority required",
                "description",
                futureDate(1),
                null,
                Set.of("validation"));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/tasks", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createTask_pastDueDate_returnsBadRequest() {
        TaskCreateDto request = new TaskCreateDto(
                "Past due date",
                "description",
                LocalDateTime.now().minusDays(1).withNano(0),
                Priority.MEDIUM,
                Set.of("validation"));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/tasks", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getTask_returnsExistingTask() {
        TaskCreateDto request = createRequest("Get me", "Get test");
        ResponseEntity<TaskResponseDto> created = restTemplate.postForEntity(
                "/api/tasks", request, TaskResponseDto.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().id();

        ResponseEntity<TaskResponseDto> response = restTemplate.getForEntity(
                "/api/tasks/" + id, TaskResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("Get me", response.getBody().title());
    }

    @Test
    void getTask_nonExistentId_returnsNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/tasks/999999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllTasks_returnsNonEmptyList() {
        ResponseEntity<List<TaskResponseDto>> response = restTemplate.exchange(
                "/api/tasks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void updateTask_returnsPatchedResponseAndKeepsMissingFields() {
        TaskCreateDto createReq = new TaskCreateDto(
                "Old title",
                "Old desc",
                futureDate(3),
                Priority.HIGH,
                Set.of("backend", "api"));

        ResponseEntity<TaskResponseDto> created = restTemplate.postForEntity(
                "/api/tasks", createReq, TaskResponseDto.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().id();

        TaskUpdateDto updateReq = new TaskUpdateDto(
                "New title",
                null,
                true,
                null,
                null,
                null);

        ResponseEntity<TaskResponseDto> response = restTemplate.exchange(
                "/api/tasks/" + id,
                HttpMethod.PATCH,
                new HttpEntity<>(updateReq),
                TaskResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        TaskResponseDto body = response.getBody();
        assertNotNull(body);
        assertEquals(id, body.id());
        assertEquals("New title", body.title());
        assertEquals("Old desc", body.description());
        assertTrue(body.completed());
        assertEquals(createReq.getDueDate(), body.dueDate());
        assertEquals(createReq.getPriority(), body.priority());
        assertEquals(createReq.getTags(), body.tags());
    }

    @Test
    void updateTask_blankTitle_returnsBadRequest() {
        TaskCreateDto createReq = createRequest("Valid", "Valid desc");
        ResponseEntity<TaskResponseDto> created = restTemplate.postForEntity(
                "/api/tasks", createReq, TaskResponseDto.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().id();

        TaskUpdateDto updateReq = new TaskUpdateDto("", "New desc", false, null, null, null);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tasks/" + id,
                HttpMethod.PATCH,
                new HttpEntity<>(updateReq),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateTask_nonExistentId_returnsNotFound() {
        TaskUpdateDto updateReq = new TaskUpdateDto("Title", "Desc", false, null, null, null);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tasks/999999",
                HttpMethod.PATCH,
                new HttpEntity<>(updateReq),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteTask_returnsNoContent() {
        TaskCreateDto createReq = createRequest("To delete", "Will be removed");
        ResponseEntity<TaskResponseDto> created = restTemplate.postForEntity(
                "/api/tasks", createReq, TaskResponseDto.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().id();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/" + id,
                HttpMethod.DELETE,
                null,
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "/api/tasks/" + id, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void deleteTask_nonExistentId_returnsNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tasks/999999",
                HttpMethod.DELETE,
                null,
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void statistics_returnsOkWithComparison() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/statistics", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("primary="));
        assertTrue(response.getBody().contains("stub="));
    }

    private TaskCreateDto createRequest(String title, String description) {
        return new TaskCreateDto(
                title,
                description,
                futureDate(1),
                Priority.MEDIUM,
                Set.of("study", "api"));
    }

    private LocalDateTime futureDate(int days) {
        return LocalDateTime.now().plusDays(days).withNano(0);
    }
}
