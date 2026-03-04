package com.mipt.nikitabumagin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mipt.nikitabumagin.dto.TaskDto;
import com.mipt.nikitabumagin.model.Task;
import java.util.List;
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
 * <p>Uses {@link TestRestTemplate} to perform real HTTP calls against the
 * running application context. Each endpoint has at least one positive and one negative
 * scenario.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createTask_returnsCreatedStatus() {
        TaskDto request = new TaskDto("Test task", "Some description", false);

        ResponseEntity<Task> response = restTemplate.postForEntity(
                "/api/tasks", request, Task.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Task body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());
        assertEquals("Test task", body.getTitle());
        assertEquals("Some description", body.getDescription());
        assertFalse(body.isCompleted());
        assertTrue(response.getHeaders().containsKey("Location"));
    }

    @Test
    void createTask_blankTitle_returnsBadRequest() {
        TaskDto request = new TaskDto("", "description", false);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/tasks", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getTask_returnsExistingTask() {
        TaskDto request = new TaskDto("Get me", "Get test", false);
        ResponseEntity<Task> created = restTemplate.postForEntity(
                "/api/tasks", request, Task.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().getId();

        ResponseEntity<Task> response = restTemplate.getForEntity(
                "/api/tasks/" + id, Task.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().getId());
        assertEquals("Get me", response.getBody().getTitle());
    }

    @Test
    void getTask_nonExistentId_returnsNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/tasks/999999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllTasks_returnsNonEmptyList() {
        restTemplate.postForEntity(
                "/api/tasks", new TaskDto("List task", "desc"), Task.class);

        ResponseEntity<List<Task>> response = restTemplate.exchange(
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
    void getAllTasks_returnsOkEvenIfEmpty() {
        ResponseEntity<List<Task>> response = restTemplate.exchange(
                "/api/tasks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateTask_returnsUpdatedTask() {
        TaskDto createReq = new TaskDto("Old title", "Old desc", false);
        ResponseEntity<Task> created = restTemplate.postForEntity(
                "/api/tasks", createReq, Task.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().getId();

        TaskDto updateReq = new TaskDto("New title", "New desc", true);
        ResponseEntity<Task> response = restTemplate.exchange(
                "/api/tasks/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(updateReq),
                Task.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Task body = response.getBody();
        assertNotNull(body);
        assertEquals("New title", body.getTitle());
        assertEquals("New desc", body.getDescription());
        assertTrue(body.isCompleted());
    }

    @Test
    void updateTask_nonExistentId_returnsNotFound() {
        TaskDto updateReq = new TaskDto("Title", "Desc", false);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tasks/999999",
                HttpMethod.PUT,
                new HttpEntity<>(updateReq),
                String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteTask_returnsNoContent() {
        TaskDto createReq = new TaskDto("To delete", "Will be removed", false);
        ResponseEntity<Task> created = restTemplate.postForEntity(
                "/api/tasks", createReq, Task.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().getId();

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
    void createTask_nullTitle_returnsBadRequest() {
        TaskDto request = new TaskDto(null, "description", false);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/tasks", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createTask_titleTooLong_returnsBadRequest() {
        String longTitle = "A".repeat(101);
        TaskDto request = new TaskDto(longTitle, "description", false);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/tasks", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateTask_blankTitle_returnsBadRequest() {
        TaskDto createReq = new TaskDto("Valid", "Valid desc", false);
        ResponseEntity<Task> created = restTemplate.postForEntity(
                "/api/tasks", createReq, Task.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().getId();

        TaskDto updateReq = new TaskDto("", "New desc", false);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tasks/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(updateReq),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteTask_twice_secondDeleteReturnsNotFound() {
        TaskDto createReq = new TaskDto("Double delete", "Will be deleted twice", false);
        ResponseEntity<Task> created = restTemplate.postForEntity(
                "/api/tasks", createReq, Task.class);

        assertNotNull(created.getBody());
        Long id = created.getBody().getId();

        ResponseEntity<Void> first = restTemplate.exchange(
                "/api/tasks/" + id, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, first.getStatusCode());

        ResponseEntity<String> second = restTemplate.exchange(
                "/api/tasks/" + id, HttpMethod.DELETE, null, String.class);
        assertEquals(HttpStatus.NOT_FOUND, second.getStatusCode());
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
}
