package com.mipt.nikitabumagin.dto.v1;

public record UnstableProbeResponseDto(
        String mode,
        boolean fallback,
        String message
) {
}
