package com.mipt.nikitabumagin.config;

import com.mipt.nikitabumagin.repository.StubTaskRepository;
import com.mipt.nikitabumagin.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskRepositoryConfig {

    @Bean
    public TaskRepository stubTaskRepository() {
        return new StubTaskRepository();
    }
}
