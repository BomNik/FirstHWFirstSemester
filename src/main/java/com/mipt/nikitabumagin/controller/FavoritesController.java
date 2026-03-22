package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.dto.TaskResponseDto;
import com.mipt.nikitabumagin.dto.ErrorResponse;
import com.mipt.nikitabumagin.service.FavoritesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
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
@Tag(name = "Favorites", description = "Session-based operations for favorite tasks")
public class FavoritesController {

    private static final Logger log = LoggerFactory.getLogger(FavoritesController.class);

    private final FavoritesService favoritesService;

    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @Operation(
            summary = "Add task to favorites",
            description = "Stores the task id in the current HTTP session favorites."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Task added to favorites",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task id is invalid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/{taskId}")
    public ResponseEntity<Void> addToFavorites(
            @Parameter(description = "Task identifier", required = true, example = "1")
            @PathVariable @Min(1) Long taskId,
            @Parameter(hidden = true) HttpSession session) {
        favoritesService.addToFavorites(taskId, session);
        log.info("Favorites updated: sessionId={}, action=add, taskId={}, favoriteTaskIds={}",
                session.getId(),
                taskId,
                favoritesService.getFavoriteTaskIds(session));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remove task from favorites",
            description = "Removes the task id from the current HTTP session favorites."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Task removed from favorites",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task id is invalid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> removeFromFavorites(
            @Parameter(description = "Task identifier", required = true, example = "1")
            @PathVariable @Min(1) Long taskId,
            @Parameter(hidden = true) HttpSession session) {
        favoritesService.removeFromFavorites(taskId, session);
        log.info("Favorites updated: sessionId={}, action=remove, taskId={}, favoriteTaskIds={}",
                session.getId(),
                taskId,
                favoritesService.getFavoriteTaskIds(session));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get favorite tasks",
            description = "Returns tasks marked as favorites in the current HTTP session."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Favorite tasks returned successfully",
                    headers = @Header(
                            name = "X-Total-Count",
                            description = "Number of favorite tasks in the response",
                            schema = @Schema(type = "integer", example = "2")
                    ),
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getFavorites(
            @Parameter(hidden = true) HttpSession session) {
        List<TaskResponseDto> favorites = favoritesService.getFavoriteTasks(session);
        log.info("Favorites requested: sessionId={}, favoriteTaskIds={}, favoriteCount={}",
                session.getId(),
                favoritesService.getFavoriteTaskIds(session),
                favorites.size());
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(favorites.size()))
                .body(favorites);
    }
}
