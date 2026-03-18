package com.mipt.nikitabumagin.dto;

import java.time.LocalDateTime;

public record TaskAttachmentResponseDto(
        Long id,
        Long taskId,
        String fileName,
        String contentType,
        Long size,
        LocalDateTime uploadedAt
) {

}
