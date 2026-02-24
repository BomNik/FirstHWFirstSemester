package com.mipt.nikitabumagin.repository;

import com.mipt.nikitabumagin.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task create(String title, String description, Boolean completed);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    Task update(Task task);

    boolean deleteById(Long id);
}
