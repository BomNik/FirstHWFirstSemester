package com.mipt.nikitabumagin.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskAttachmentUpdateDto {

    @NotBlank
    String fileName;

    @NotBlank
    String contentType;
}
