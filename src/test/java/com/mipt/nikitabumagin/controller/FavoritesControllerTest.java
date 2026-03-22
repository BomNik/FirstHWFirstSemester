package com.mipt.nikitabumagin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mipt.nikitabumagin.dto.mapper.TaskMapper;
import com.mipt.nikitabumagin.model.Priority;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.repository.TaskRepository;
import com.mipt.nikitabumagin.service.FavoritesService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FavoritesControllerTest {

    private MockMvc mockMvc;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        TestTaskRepository repository = new TestTaskRepository();
        repository.create(task("First favorite", "backend"));
        repository.create(task("Second favorite", "frontend"));

        FavoritesService favoritesService = new FavoritesService(
                repository,
                Mappers.getMapper(TaskMapper.class));
        FavoritesController controller = new FavoritesController(favoritesService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        session = new MockHttpSession();
    }

    @Test
    void addAndListFavorites_persistsIdsInsideHttpSession() throws Exception {
        mockMvc.perform(post("/api/favorites/{taskId}", 1L).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/favorites/{taskId}", 2L).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("First favorite"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Second favorite"));
    }

    @Test
    void addSameTaskTwice_doesNotDuplicateFavorite() throws Exception {
        mockMvc.perform(post("/api/favorites/{taskId}", 1L).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/favorites/{taskId}", 1L).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void removeFromFavorites_deletesOnlyFromCurrentSession() throws Exception {
        mockMvc.perform(post("/api/favorites/{taskId}", 1L).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/favorites/{taskId}", 1L).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addMissingTask_returnsNotFound() throws Exception {
        mockMvc.perform(post("/api/favorites/{taskId}", 999L).session(session))
                .andExpect(status().isNotFound());
    }

    private Task task(String title, String tag) {
        return Task.builder()
                .title(title)
                .description("Description for " + title)
                .completed(false)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(Priority.MEDIUM)
                .tags(Set.of(tag))
                .build();
    }

    private static class TestTaskRepository implements TaskRepository {

        private final Map<Long, Task> storage = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Task create(Task task) {
            long id = sequence.incrementAndGet();
            task.setId(id);
            storage.put(id, task);
            return task;
        }

        @Override
        public Optional<Task> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Task> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public Task update(Task task) {
            storage.put(task.getId(), task);
            return task;
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }
}
