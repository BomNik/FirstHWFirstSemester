package com.mipt.nikitabumagin.service;

import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.mapper.TaskMapper;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import com.mipt.nikitabumagin.model.Task;
import com.mipt.nikitabumagin.repository.TaskRepository;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class FavoritesService {

    static final String FAVORITE_TASK_IDS_ATTRIBUTE = "favoriteTaskIds";

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public FavoritesService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public void addToFavorites(Long taskId, HttpSession session) {
        ensureTaskExists(taskId);
        getOrCreateFavoriteTaskIds(session).add(taskId);
    }

    public void removeFromFavorites(Long taskId, HttpSession session) {
        getOrCreateFavoriteTaskIds(session).remove(taskId);
    }

    public List<Long> getFavoriteTaskIds(HttpSession session) {
        return List.copyOf(getOrCreateFavoriteTaskIds(session));
    }

    public List<TaskResponseDto> getFavoriteTasks(HttpSession session) {
        LinkedHashSet<Long> favoriteTaskIds = getOrCreateFavoriteTaskIds(session);
        List<Long> staleIds = new ArrayList<>();
        List<TaskResponseDto> favorites = new ArrayList<>();

        for (Long taskId : favoriteTaskIds) {
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                staleIds.add(taskId);
                continue;
            }
            favorites.add(taskMapper.toResponseDto(task));
        }

        if (!staleIds.isEmpty()) {
            favoriteTaskIds.removeAll(staleIds);
            session.setAttribute(FAVORITE_TASK_IDS_ATTRIBUTE, favoriteTaskIds);
        }

        return favorites;
    }

    private void ensureTaskExists(Long taskId) {
        taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @SuppressWarnings("unchecked")
    private LinkedHashSet<Long> getOrCreateFavoriteTaskIds(HttpSession session) {
        Object attribute = session.getAttribute(FAVORITE_TASK_IDS_ATTRIBUTE);

        if (attribute instanceof LinkedHashSet<?>) {
            return (LinkedHashSet<Long>) attribute;
        }

        if (attribute instanceof Set<?> existingSet) {
            LinkedHashSet<Long> normalized = new LinkedHashSet<>();
            for (Object id : existingSet) {
                if (id instanceof Long longId) {
                    normalized.add(longId);
                }
            }
            session.setAttribute(FAVORITE_TASK_IDS_ATTRIBUTE, normalized);
            return normalized;
        }

        LinkedHashSet<Long> favoriteTaskIds = new LinkedHashSet<>();
        session.setAttribute(FAVORITE_TASK_IDS_ATTRIBUTE, favoriteTaskIds);
        return favoriteTaskIds;
    }
}
