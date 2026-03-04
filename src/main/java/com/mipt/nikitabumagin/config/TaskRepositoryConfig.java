package com.mipt.nikitabumagin.config;

import com.mipt.nikitabumagin.repository.StubTaskRepository;
import com.mipt.nikitabumagin.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that declares additional
 * {@link com.mipt.nikitabumagin.repository.TaskRepository} bean definitions.
 *
 * <p>Registers a {@link com.mipt.nikitabumagin.repository.StubTaskRepository} as a named bean
 * so that it can be injected alongside the primary in-memory implementation using the
 * {@code @Qualifier("stubTaskRepository")} annotation.</p>
 */
@Configuration
public class TaskRepositoryConfig {

    @Bean
    public TaskRepository stubTaskRepository() {
        return new StubTaskRepository();
    }
}
