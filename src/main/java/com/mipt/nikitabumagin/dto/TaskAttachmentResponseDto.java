package com.mipt.nikitabumagin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response body with attachment metadata")
public record TaskAttachmentResponseDto(
        @Schema(description = "Attachment identifier", example = "1")
        Long id,
        @Schema(description = "Identifier of the task that owns the attachment", example = "1")
        Long taskId,
        @Schema(description = "Original uploaded file name", example = "notes.txt")
        String fileName,
        @Schema(description = "Detected file content type", example = "text/plain")
        String contentType,
        @Schema(description = "Attachment size in bytes", example = "512")
        Long size,
        @Schema(description = "Attachment upload timestamp", example = "2026-03-22T11:30:00")
        LocalDateTime uploadedAt
) {

}
