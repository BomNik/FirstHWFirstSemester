package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.dto.v1.DocsResponseDto;
import com.mipt.nikitabumagin.dto.v1.ProfileResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Protected resources for v1 API.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Protected Resources v1", description = "Protected endpoints for role/authority checks")
public class V1ProtectedContentController {

    @Operation(
            summary = "Profile endpoint",
            description = "Will be protected by hasRole('USER') in security configuration."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile returned",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProfileResponseDto.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDto> profile(Authentication authentication) {
        List<String> allAuthorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        List<String> roles = allAuthorities.stream()
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();

        List<String> nonRoleAuthorities = allAuthorities.stream()
                .filter(authority -> !authority.startsWith("ROLE_"))
                .toList();

        ProfileResponseDto response = new ProfileResponseDto(
                authentication.getName(),
                roles,
                nonRoleAuthorities
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Docs endpoint",
            description = "Will be protected by hasAuthority('READ_PRIVILEGE') in security configuration."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Docs metadata returned",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DocsResponseDto.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Insufficient authority")
    })
    @GetMapping("/docs")
    public ResponseEntity<DocsResponseDto> docs() {
        DocsResponseDto response = new DocsResponseDto(
                "Protected docs endpoint contract is in place",
                List.of("READ_PRIVILEGE")
        );
        return ResponseEntity.ok(response);
    }
}
