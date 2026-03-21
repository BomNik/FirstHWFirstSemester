package com.mipt.nikitabumagin.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PreferencesControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PreferencesController()).build();
    }

    @Test
    void getViewPreference_withoutCookie_returnsDefaultCompactMode() throws Exception {
        mockMvc.perform(get("/api/preferences/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("compact"));
    }

    @Test
    void getViewPreference_readsCookieValue() throws Exception {
        mockMvc.perform(get("/api/preferences/view")
                        .cookie(new Cookie("viewPreference", "detailed")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("detailed"));
    }

    @Test
    void updateViewPreference_setsCookieAndReturnsCurrentMode() throws Exception {
        mockMvc.perform(post("/api/preferences/view").param("mode", "detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("detailed"))
                .andExpect(header().string("Set-Cookie", containsString("viewPreference=detailed")))
                .andExpect(cookie().exists("viewPreference"));
    }

    @Test
    void updateViewPreference_invalidMode_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/preferences/view").param("mode", "grid"))
                .andExpect(status().isBadRequest());
    }
}
