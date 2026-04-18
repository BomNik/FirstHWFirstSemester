package com.mipt.nikitabumagin.dto.v1;

public record GatewayTaskResponseDto(
        Long id,
        String title,
        String description,
        boolean completed
) {
}
