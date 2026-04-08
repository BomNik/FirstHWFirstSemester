package com.mipt.nikitabumagin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskAttachmentCreateDto(
        @NotNull Long taskId,
        @NotBlank String fileName,
        @NotBlank String contentType,
        @NotNull Long size
) {

}
