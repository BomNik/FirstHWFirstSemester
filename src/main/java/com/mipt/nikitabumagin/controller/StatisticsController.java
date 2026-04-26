package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.service.TaskStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes repository comparison statistics.
 *
 * <p>Demonstrates the use of {@link TaskStatisticsService}, which internally
 * relies on {@link org.springframework.beans.factory.annotation.Qualifier @Qualifier} to inject
 * both the primary and stub {@link com.mipt.nikitabumagin.repository.TaskRepository}
 * implementations simultaneously.</p>
 *
 * @see com.mipt.nikitabumagin.service.TaskStatisticsService
 */
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics", description = "Read-only statistics and demo metrics")
public class StatisticsController {

    private final TaskStatisticsService statisticsService;

    public StatisticsController(TaskStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Returns a comparison of the number of tasks stored in the primary (in-memory) repository
     * versus the stub repository.
     */
    @Operation(
            summary = "Compare repositories",
            description = "Returns a short comparison string for the primary and stub task repositories."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics returned successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object",
                                    example = "{\"comparison\":\"primary=3, stub=0\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error"
            )
    })
    @GetMapping
    public ResponseEntity<Map<String, String>> compare() {
        return ResponseEntity.ok(Map.of("comparison", statisticsService.compareRepositories()));
    }
}
