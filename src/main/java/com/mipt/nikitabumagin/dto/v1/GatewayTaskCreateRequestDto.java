package com.mipt.nikitabumagin.dto.v1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GatewayTaskCreateRequestDto(
        @NotBlank(message = "title must not be blank")
        @Size(max = 120, message = "title must be <= 120 chars")
        String title,
        @Size(max = 1000, message = "description must be <= 1000 chars")
        String description,
        Boolean completed
) {
}
