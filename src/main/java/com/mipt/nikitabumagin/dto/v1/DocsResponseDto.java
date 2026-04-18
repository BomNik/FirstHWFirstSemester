package com.mipt.nikitabumagin.dto.v1;

import java.util.List;

public record DocsResponseDto(
        String message,
        List<String> requiredAuthorities
) {
}
