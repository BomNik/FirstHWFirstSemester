package com.mipt.nikitabumagin.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.nikitabumagin.controller.PreferencesController;
import com.mipt.nikitabumagin.controller.TaskController;
import com.mipt.nikitabumagin.dto.TaskCreateDto;
import com.mipt.nikitabumagin.service.TaskService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Validator validator;
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();

        LocalValidatorFactoryBean springValidator = new LocalValidatorFactoryBean();
        springValidator.afterPropertiesSet();

        validator = Validation.buildDefaultValidatorFactory().getValidator();
        TaskService taskService = mock(TaskService.class);
        globalExceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new TaskController(taskService),
                        new PreferencesController(),
                        new TestExceptionController(validator)
                )
                .setControllerAdvice(globalExceptionHandler)
                .setValidator(springValidator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void taskNotFound_returnsUniform404ErrorResponse() throws Exception {
        mockMvc.perform(get("/test/task-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found: id=999"))
                .andExpect(jsonPath("$.path").value("/test/task-not-found"));
    }

    @Test
    void methodArgumentNotValid_returnsFieldErrorsInDetails() throws Exception {
        TaskCreateDto request = new TaskCreateDto(
                "",
                "Description",
                LocalDateTime.now().plusDays(1).withNano(0),
                null,
                Set.of("validation")
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.fieldErrors.title").exists())
                .andExpect(jsonPath("$.details.fieldErrors.priority").exists())
                .andExpect(jsonPath("$.path").value("/api/tasks"));
    }

    @Test
    void methodArgumentNotValid_returnsDueDateFieldErrorWhenDueDateIsInPast() throws Exception {
        TaskCreateDto request = new TaskCreateDto(
                "Valid title",
                "Description",
                LocalDateTime.now().minusDays(1).withNano(0),
                com.mipt.nikitabumagin.model.Priority.MEDIUM,
                Set.of("validation")
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.fieldErrors.dueDate").exists())
                .andExpect(jsonPath("$.path").value("/api/tasks"));
    }

    @Test
    void constraintViolation_returnsValidationDetailsForPathVariable() throws Exception {
        mockMvc.perform(get("/test/constraint"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Constraint validation failed"))
                .andExpect(jsonPath("$.details.violations[0].property").value("value"))
                .andExpect(jsonPath("$.details.violations[0].message",
                        containsString("must be greater than or equal to 1")));
    }

    @Test
    void missingServletRequestParameter_returnsBadRequestErrorResponse() throws Exception {
        mockMvc.perform(post("/api/preferences/view"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Required request parameter is missing"))
                .andExpect(jsonPath("$.details.parameter").value("mode"))
                .andExpect(jsonPath("$.path").value("/api/preferences/view"));
    }

    @Test
    void malformedJson_returnsBadRequestErrorResponse() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"))
                .andExpect(jsonPath("$.details.cause").exists())
                .andExpect(jsonPath("$.path").value("/api/tasks"));
    }

    @Test
    void responseStatusException_returnsUniformErrorResponse() throws Exception {
        mockMvc.perform(post("/api/preferences/view").param("mode", "grid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Unsupported view mode: grid"))
                .andExpect(jsonPath("$.path").value("/api/preferences/view"));
    }

    @Test
    void illegalArgumentException_returnsBadRequestErrorResponse() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Attachment file must not be empty"))
                .andExpect(jsonPath("$.path").value("/test/illegal-argument"));
    }

    @Test
    void unexpectedException_returnsInternalServerErrorWithoutStackTrace() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.path").value("/test/boom"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void noHandlerFound_buildsUniform404ErrorResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/does-not-exist");
        NoHandlerFoundException ex = new NoHandlerFoundException(
                "GET",
                "/api/does-not-exist",
                HttpHeaders.EMPTY
        );

        ResponseEntity<com.mipt.nikitabumagin.dto.ErrorResponse> response =
                globalExceptionHandler.handleNoHandlerFound(ex, request);

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("No handler found for GET /api/does-not-exist",
                response.getBody().message());
        assertEquals("/api/does-not-exist", response.getBody().path());
    }

    @Test
    void invalidTask_buildsUniform400ErrorResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/tasks");

        ResponseEntity<com.mipt.nikitabumagin.dto.ErrorResponse> response =
                globalExceptionHandler.handleInvalidTask(new InvalidTaskException("Invalid payload"), request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Invalid payload", response.getBody().message());
        assertEquals("/api/tasks", response.getBody().path());
    }

    @Test
    void attachmentNotFound_buildsUniform404ErrorResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/attachments/42");

        ResponseEntity<com.mipt.nikitabumagin.dto.ErrorResponse> response =
                globalExceptionHandler.handleAttachmentNotFound(
                        new AttachmentNotFoundException(42L),
                        request
                );

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Attachment not found: id=42", response.getBody().message());
        assertEquals("/api/attachments/42", response.getBody().path());
    }

    @RestController
    @RequestMapping("/test")
    private static class TestExceptionController {

        private final Validator validator;

        private TestExceptionController(Validator validator) {
            this.validator = validator;
        }

        @GetMapping("/task-not-found")
        String taskNotFound() {
            throw new TaskNotFoundException(999L);
        }

        @GetMapping("/constraint")
        String constraint() {
            TestInput invalidInput = new TestInput(0);
            throw new ConstraintViolationException(validator.validate(invalidInput));
        }

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("boom");
        }

        @GetMapping("/illegal-argument")
        String illegalArgument() {
            throw new IllegalArgumentException("Attachment file must not be empty");
        }
    }

    private record TestInput(@Min(1) int value) {
    }
}
