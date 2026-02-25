package com.mipt.nikitabumagin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a task's fields fail business-level validation (e.g. blank title, null
 * description).
 *
 * <p>Mapped to HTTP {@code 400 Bad Request} via {@link ResponseStatus}.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTaskException extends RuntimeException {

    public InvalidTaskException(String message) {
        super(message);
    }
}
