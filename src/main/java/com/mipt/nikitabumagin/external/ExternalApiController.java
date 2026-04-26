package com.mipt.nikitabumagin.external;

import com.mipt.nikitabumagin.dto.v1.GatewayTaskCreateRequestDto;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Local emulator of an external tasks API for debugging RestClient and resilience logic.
 */
@RestController
@RequestMapping("/external/v1")
@Tag(name = "External API Emulator", description = "Mock external tasks API")
public class ExternalApiController {

    private final Map<Long, GatewayTaskResponseDto> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1000);

    @PostMapping("/tasks")
    public ResponseEntity<GatewayTaskResponseDto> createTask(
            @Valid @RequestBody GatewayTaskCreateRequestDto request) {
        long id = sequence.incrementAndGet();
        GatewayTaskResponseDto created = new GatewayTaskResponseDto(
                id,
                request.title(),
                request.description(),
                request.completed() != null && request.completed()
        );
        storage.put(id, created);
        return ResponseEntity
                .created(URI.create("/external/v1/tasks/" + id))
                .body(created);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        GatewayTaskResponseDto task = storage.get(id);
        if (task == null) {
            return notFoundTask(id);
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<GatewayTaskResponseDto>> listTasks(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(defaultValue = "20") Integer limit) {
        int normalizedLimit = Math.max(limit, 1);

        List<GatewayTaskResponseDto> response = storage.values().stream()
                .filter(task -> completed == null || task.completed() == completed)
                .sorted(Comparator.comparing(GatewayTaskResponseDto::id))
                .limit(normalizedLimit)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> replaceTask(
            @PathVariable Long id,
            @Valid @RequestBody GatewayTaskCreateRequestDto request) {
        GatewayTaskResponseDto existing = storage.get(id);
        if (existing == null) {
            return notFoundTask(id);
        }
        GatewayTaskResponseDto updated = new GatewayTaskResponseDto(
                id,
                request.title(),
                request.description(),
                Optional.ofNullable(request.completed()).orElse(existing.completed())
        );
        storage.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        GatewayTaskResponseDto removed = storage.remove(id);
        if (removed == null) {
            return notFoundTask(id);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unstable")
    public ResponseEntity<?> unstable(@RequestParam String mode) {
        return switch (mode) {
            case "timeout" -> simulateTimeout();
            case "500" -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forStatusAndDetail(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Simulated external 500 error"
                    ));
            case "429" -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, "5")
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forStatusAndDetail(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "Simulated external rate limit"
                    ));
            case "html" -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<html><body><h1>502 Bad Gateway</h1></body></html>");
            default -> ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forStatusAndDetail(
                            HttpStatus.BAD_REQUEST,
                            "Unknown mode. Expected one of: timeout, 500, 429, html"
                    ));
        };
    }

    private ResponseEntity<?> simulateTimeout() {
        try {
            Thread.sleep(3500L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(ProblemDetail.forStatusAndDetail(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Timeout simulation interrupted"
                    ));
        }
        return ResponseEntity.ok(Map.of("status", "late-response"));
    }

    private ResponseEntity<ProblemDetail> notFoundTask(Long id) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Task Not Found");
        problem.setDetail("External task not found: id=" + id);
        problem.setProperty("taskId", id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
