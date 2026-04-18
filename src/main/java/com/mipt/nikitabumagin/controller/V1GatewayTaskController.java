package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.dto.v1.GatewayTaskCreateRequestDto;
import com.mipt.nikitabumagin.dto.v1.GatewayTaskResponseDto;
import com.mipt.nikitabumagin.service.v1.GatewayTaskContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API contract for gateway operations over tasks.
 *
 * <p>In later steps this controller will call external API via RestClient.
 */
@RestController
@Validated
@RequestMapping("/api/v1/tasks")
@Tag(name = "Gateway Tasks v1", description = "Internal gateway endpoints for external tasks API")
public class V1GatewayTaskController {

    private final GatewayTaskContractService gatewayTaskContractService;

    public V1GatewayTaskController(GatewayTaskContractService gatewayTaskContractService) {
        this.gatewayTaskContractService = gatewayTaskContractService;
    }

    @Operation(summary = "Create task via gateway")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created",
                    content = @Content(
                            schema = @Schema(implementation = GatewayTaskResponseDto.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<GatewayTaskResponseDto> createTask(
            @Valid @RequestBody GatewayTaskCreateRequestDto request) {
        GatewayTaskResponseDto created = gatewayTaskContractService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/tasks/" + created.id())).body(created);
    }

    @Operation(summary = "Get task by id via gateway")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(schema = @Schema(implementation = GatewayTaskResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GatewayTaskResponseDto> getTaskById(
            @Parameter(example = "1")
            @PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(gatewayTaskContractService.getById(id));
    }

    @Operation(summary = "List tasks via gateway")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks list returned",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = GatewayTaskResponseDto.class))
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<GatewayTaskResponseDto>> listTasks(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        List<GatewayTaskResponseDto> tasks = gatewayTaskContractService.list(completed, limit);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(tasks);
    }

    @Operation(summary = "Delete task via gateway")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(example = "1")
            @PathVariable @Min(1) Long id) {
        gatewayTaskContractService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
