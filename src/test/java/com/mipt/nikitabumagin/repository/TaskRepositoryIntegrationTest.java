package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void findTasksDueInNext7Days_shouldWorkWithPostgresContainer() {
        LocalDateTime now = LocalDateTime.now().withNano(0);

        Task inRange = Task.builder()
                .title("In range")
                .description("Due in 2 days")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(2))
                .priority(Priority.HIGH)
                .tags(Set.of("urgent"))
                .build();

        Task outOfRange = Task.builder()
                .title("Out of range")
                .description("Due in 10 days")
                .completed(false)
                .createdAt(now)
                .updatedAt(now)
                .dueDate(now.plusDays(10))
                .priority(Priority.LOW)
                .tags(Set.of("later"))
                .build();

        taskRepository.saveAll(List.of(inRange, outOfRange));

        List<Task> result = taskRepository.findTasksDueInNext7Days(now, now.plusDays(7));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("In range");
        assertThat(result.getFirst().getDueDate()).isEqualTo(now.plusDays(2));
    }
}
