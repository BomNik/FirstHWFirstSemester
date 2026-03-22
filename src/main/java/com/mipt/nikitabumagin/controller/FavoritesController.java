package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.service.FavoritesService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@Validated
public class FavoritesController {

    private static final Logger log = LoggerFactory.getLogger(FavoritesController.class);

    private final FavoritesService favoritesService;

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<Void> addToFavorites(@PathVariable @Min(1) Long taskId,
            HttpSession session) {
        favoritesService.addToFavorites(taskId, session);
        log.info("Favorites updated: sessionId={}, action=add, taskId={}, favoriteTaskIds={}",
                session.getId(),
                taskId,
                favoritesService.getFavoriteTaskIds(session));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable @Min(1) Long taskId,
            HttpSession session) {
        favoritesService.removeFromFavorites(taskId, session);
        log.info("Favorites updated: sessionId={}, action=remove, taskId={}, favoriteTaskIds={}",
                session.getId(),
                taskId,
                favoritesService.getFavoriteTaskIds(session));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
        List<TaskResponseDto> favorites = favoritesService.getFavoriteTasks(session);
        log.info("Favorites requested: sessionId={}, favoriteTaskIds={}, favoriteCount={}",
                session.getId(),
                favoritesService.getFavoriteTaskIds(session),
                favorites.size());
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(favorites.size()))
                .body(favorites);
    }
}
