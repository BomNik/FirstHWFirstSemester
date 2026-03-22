package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.dto.ErrorResponse;
import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.TaskUpdateDto;
import com.mipt.nikitabumagin.service.TaskService;
import com.mipt.nikitabumagin.validation.OnCreate;
import com.mipt.nikitabumagin.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes CRUD endpoints for task management.
 *
 * <p>All request/response bodies are validated using Jakarta Bean Validation.
 * Endpoints:
 * <ul>
 *   <li>{@code POST   /api/tasks}       — create a new task</li>
 *   <li>{@code GET    /api/tasks/{id}}   — retrieve a task by its identifier</li>
 *   <li>{@code GET    /api/tasks}        — list all tasks</li>
 *   <li>{@code PATCH    /api/tasks/{id}}   — update an existing task</li>
 *   <li>{@code DELETE /api/tasks/{id}}   — delete a task</li>
 * </ul>
 *
 * @see com.mipt.nikitabumagin.service.TaskService
 */
@RestController
@RequestMapping("/api/tasks")
@Validated
@Tag(name = "Tasks", description = "Operations for creating, reading, updating, and deleting tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(
            summary = "Get tasks list",
            description = "Returns all tasks. The response includes the X-Total-Count header "
                    + "with the total number of returned tasks."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks returned successfully",
                    headers = @Header(
                            name = "X-Total-Count",
                            description = "Number of tasks in the response",
                            schema = @Schema(type = "integer", example = "3")
                    ),
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        List<TaskResponseDto> tasks = taskService.getAllTasks();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(tasks.size()))
                .body(tasks);
    }

    @Operation(
            summary = "Create task",
            description = "Creates a new task and returns its persisted representation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Task payload used to create a new task"
    )
    public ResponseEntity<TaskResponseDto> createTask(
            @RequestBody @Validated(OnCreate.class) TaskCreateDto request) {
        TaskResponseDto created = taskService.createTask(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/tasks/" + created.id())
                .body(created);
    }

    @Operation(
            summary = "Get task by id",
            description = "Returns a single task by its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task returned successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task id is invalid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTask(
            @Parameter(description = "Task identifier", required = true, example = "1")
            @PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(
            summary = "Update task",
            description = "Partially updates an existing task. Fields omitted in the request keep "
                    + "their current values."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task id or request body is invalid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PatchMapping("/{id}")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Fields to update for the task"
    )
    public ResponseEntity<TaskResponseDto> updateTask(
            @Parameter(description = "Task identifier", required = true, example = "1")
            @PathVariable @Min(1) Long id,
            @RequestBody @Validated(OnUpdate.class) TaskUpdateDto update) {
        return ResponseEntity.ok(taskService.updateTask(id, update));
    }

    @Operation(
            summary = "Delete task",
            description = "Deletes a task by its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Task deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task id is invalid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task identifier", required = true, example = "1")
            @PathVariable @Min(1) Long id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
