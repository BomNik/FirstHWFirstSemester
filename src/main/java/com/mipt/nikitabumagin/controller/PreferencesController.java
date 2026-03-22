package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Preferences", description = "Cookie-based user view preferences")
public class PreferencesController {

    private static final Logger log = LoggerFactory.getLogger(PreferencesController.class);
    private static final String VIEW_PREFERENCE_COOKIE = "viewPreference";
    private static final String DEFAULT_VIEW_MODE = "compact";
    private static final Set<String> SUPPORTED_VIEW_MODES = Set.of("compact", "detailed");

    @Operation(
            summary = "Get view preference",
            description = "Reads the current UI view mode from the viewPreference cookie. "
                    + "If the cookie is absent, the compact mode is returned."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current view preference returned successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object", example = "{\"mode\":\"compact\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cookie contains an unsupported view mode",
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
    @GetMapping("/view")
    public ResponseEntity<Map<String, String>> getViewPreference(
            @Parameter(
                    name = VIEW_PREFERENCE_COOKIE,
                    in = ParameterIn.COOKIE,
                    description = "Current UI view mode stored in a cookie",
                    schema = @Schema(
                            allowableValues = {"compact", "detailed"},
                            defaultValue = DEFAULT_VIEW_MODE
                    )
            )
            @CookieValue(name = VIEW_PREFERENCE_COOKIE, defaultValue = DEFAULT_VIEW_MODE)
            String mode) {
        String normalizedMode = normalizeMode(mode);
        log.info("View preference read: cookieName={}, rawValue={}, resolvedValue={}",
                VIEW_PREFERENCE_COOKIE,
                mode,
                normalizedMode);
        return ResponseEntity.ok(Map.of("mode", normalizedMode));
    }

    @Operation(
            summary = "Update view preference",
            description = "Validates the requested view mode and stores it in the viewPreference cookie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "View preference updated successfully",
                    headers = @Header(
                            name = HttpHeaders.SET_COOKIE,
                            description = "Updated viewPreference cookie"
                    ),
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object", example = "{\"mode\":\"detailed\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Query parameter is missing or has an unsupported value",
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
    @PostMapping("/view")
    public ResponseEntity<Map<String, String>> updateViewPreference(
            @Parameter(
                    description = "Requested UI view mode",
                    required = true,
                    example = "detailed",
                    schema = @Schema(allowableValues = {"compact", "detailed"})
            )
            @RequestParam String mode) {
        String normalizedMode = normalizeMode(mode);
        ResponseCookie cookie = ResponseCookie.from(VIEW_PREFERENCE_COOKIE, normalizedMode)
                .httpOnly(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(30))
                .build();
        log.info("View preference updated: cookieName={}, newValue={}, maxAgeDays={}",
                VIEW_PREFERENCE_COOKIE,
                normalizedMode,
                30);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("mode", normalizedMode));
    }

    private String normalizeMode(String mode) {
        if (mode == null) {
            return DEFAULT_VIEW_MODE;
        }

        String normalizedMode = mode.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_VIEW_MODES.contains(normalizedMode)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported view mode: " + mode);
        }
        return normalizedMode;
    }
}
