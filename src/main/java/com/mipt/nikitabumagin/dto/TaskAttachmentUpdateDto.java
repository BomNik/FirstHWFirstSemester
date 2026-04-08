package com.mipt.nikitabumagin.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskAttachmentUpdateDto(
        @NotBlank String fileName,
        @NotBlank String contentType
) {

}
