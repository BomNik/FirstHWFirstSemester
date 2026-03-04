package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.service.TaskStatisticsService;
import java.util.Map;
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
public class StatisticsController {

    private final TaskStatisticsService statisticsService;

    public StatisticsController(TaskStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Returns a comparison of the number of tasks stored in the primary (in-memory) repository
     * versus the stub repository.
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> compare() {
        return ResponseEntity.ok(Map.of("comparison", statisticsService.compareRepositories()));
    }
}
