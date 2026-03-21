package com.mipt.nikitabumagin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested attachment cannot be found in the repository.
 *
 * <p>Mapped to HTTP {@code 404 Not Found} via {@link ResponseStatus}.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AttachmentNotFoundException extends RuntimeException {

    public AttachmentNotFoundException(Long id) {
        super("Attachment not found: id=" + id);
    }
}
