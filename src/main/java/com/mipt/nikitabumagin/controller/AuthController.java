package com.mipt.nikitabumagin.controller;

import com.mipt.nikitabumagin.dto.v1.LoginRequestDto;
import com.mipt.nikitabumagin.dto.v1.LoginResponseDto;
import com.mipt.nikitabumagin.service.v1.AuthContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contract-first auth controller for v1 API.
 *
 * <p>Returns JWT access token for valid credentials.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth v1", description = "Authentication endpoints for API v1")
public class AuthController {

    private final AuthContractService authContractService;

    public AuthController(AuthContractService authContractService) {
        this.authContractService = authContractService;
    }

    @Operation(
            summary = "Login and receive access token",
            description = "Authenticates user by username/password and returns "
                    + "token response in a stable contract format."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Credentials accepted",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authContractService.login(request));
    }
}
