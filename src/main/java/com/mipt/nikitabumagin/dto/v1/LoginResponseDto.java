package com.mipt.nikitabumagin.dto.v1;

public record LoginResponseDto(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
