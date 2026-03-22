package com.mipt.nikitabumagin.filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mipt.nikitabumagin.controller.PreferencesController;
import com.mipt.nikitabumagin.exception.GlobalExceptionHandler;
import com.mipt.nikitabumagin.exception.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class ApiVersionHeaderFilterTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new PreferencesController(),
                        new TestExceptionController()
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new ApiVersionHeaderFilter("2.0.0"))
                .build();
    }

    @Test
    void addsApiVersionHeaderToSuccessfulResponse() throws Exception {
        mockMvc.perform(get("/api/preferences/view"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-API-Version", "2.0.0"));
    }

    @Test
    void addsApiVersionHeaderToErrorResponse() throws Exception {
        mockMvc.perform(get("/test/task-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-API-Version", "2.0.0"));
    }

    @RestController
    @RequestMapping("/test")
    private static class TestExceptionController {

        @GetMapping("/task-not-found")
        String taskNotFound() {
            throw new TaskNotFoundException(123L);
        }
    }
}
