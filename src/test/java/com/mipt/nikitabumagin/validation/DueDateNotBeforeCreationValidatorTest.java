package com.mipt.nikitabumagin.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mipt.nikitabumagin.model.Task;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DueDateNotBeforeCreationValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validate_allowsDueDateAfterCreation() {
        Task task = new Task();
        task.setCreatedAt(LocalDateTime.of(2026, 3, 21, 10, 0));
        task.setDueDate(LocalDateTime.of(2026, 3, 22, 10, 0));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_allowsDueDateEqualToCreation() {
        Task task = new Task();
        task.setCreatedAt(LocalDateTime.of(2026, 3, 21, 10, 0));
        task.setDueDate(LocalDateTime.of(2026, 3, 21, 10, 0));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_ignoresMissingDates() {
        Task missingDueDate = new Task();
        missingDueDate.setCreatedAt(LocalDateTime.of(2026, 3, 21, 10, 0));

        Task missingCreatedAt = new Task();
        missingCreatedAt.setDueDate(LocalDateTime.of(2026, 3, 22, 10, 0));

        assertTrue(validator.validate(missingDueDate).isEmpty());
        assertTrue(validator.validate(missingCreatedAt).isEmpty());
    }

    @Test
    void validate_rejectsDueDateBeforeCreationAndAttachesViolationToDueDate() {
        Task task = new Task();
        task.setCreatedAt(LocalDateTime.of(2026, 3, 21, 10, 0));
        task.setDueDate(LocalDateTime.of(2026, 3, 20, 10, 0));

        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        assertEquals(1, violations.size());
        ConstraintViolation<Task> violation = violations.iterator().next();
        assertEquals("dueDate", violation.getPropertyPath().toString());
        assertEquals("Due date must not be earlier than creation date", violation.getMessage());
    }
}
