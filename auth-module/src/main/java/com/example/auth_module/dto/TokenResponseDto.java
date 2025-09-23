package com.example.auth_module.dto;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Ответ со сгенерированным JWT токеном доступа.
 */
public record TokenResponseDto(
    @Schema(description = "JWT токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    @Schema(description = "Время истечения JWT (UTC)", example = "2025-09-15T22:10:00Z")
    Instant accessTokenExpiresAt
) {}
