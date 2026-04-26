package com.mipt.nikitabumagin.dto.v1;

import java.util.List;

public record ProfileResponseDto(
        String username,
        List<String> roles,
        List<String> authorities
) {
}
