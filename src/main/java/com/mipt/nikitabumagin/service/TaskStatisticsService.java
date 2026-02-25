package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service that provides comparative statistics across multiple
 * {@link com.mipt.nikitabumagin.repository.TaskRepository} implementations.
 *
 * <p>Injects both the primary (in-memory) repository and the stub repository
 * to demonstrate Spring's
 * {@link org.springframework.beans.factory.annotation.Qualifier @Qualifier}-based disambiguation
 * when multiple beans of the same type exist in the context.</p>
 */
@Service
public class TaskStatisticsService {

    private final TaskRepository primaryRepository;
    private final TaskRepository stubRepository;

    public TaskStatisticsService(
            TaskRepository primaryRepository,
            @Qualifier("stubTaskRepository") TaskRepository stubRepository) {
        this.primaryRepository = primaryRepository;
        this.stubRepository = stubRepository;
    }

    public String compareRepositories() {
        int primaryCount = primaryRepository.findAll().size();
        int stubCount = stubRepository.findAll().size();
        return "primary=" + primaryCount + ", stub=" + stubCount;
    }


}
