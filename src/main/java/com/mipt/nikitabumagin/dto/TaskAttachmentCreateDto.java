package com.mipt.nikitabumagin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskAttachmentCreateDto {

    @NotNull
    Long taskId;

    @NotBlank
    String fileName;

    @NotBlank
    String contentType;

    @NotBlank
    Long size;
}
