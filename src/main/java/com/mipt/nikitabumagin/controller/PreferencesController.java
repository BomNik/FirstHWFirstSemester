package com.mipt.nikitabumagin.controller;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
public class PreferencesController {

    private static final Logger log = LoggerFactory.getLogger(PreferencesController.class);
    private static final String VIEW_PREFERENCE_COOKIE = "viewPreference";
    private static final String DEFAULT_VIEW_MODE = "compact";
    private static final Set<String> SUPPORTED_VIEW_MODES = Set.of("compact", "detailed");

    @GetMapping("/view")
    public ResponseEntity<Map<String, String>> getViewPreference(
            @CookieValue(name = VIEW_PREFERENCE_COOKIE, defaultValue = DEFAULT_VIEW_MODE)
            String mode) {
        String normalizedMode = normalizeMode(mode);
        log.info("View preference read: cookieName={}, rawValue={}, resolvedValue={}",
                VIEW_PREFERENCE_COOKIE,
                mode,
                normalizedMode);
        return ResponseEntity.ok(Map.of("mode", normalizedMode));
    }

    @PostMapping("/view")
    public ResponseEntity<Map<String, String>> updateViewPreference(@RequestParam String mode) {
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
