package com.example.auth_module.dto;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Ответ со сгенерированным JWT токеном доступа.
 */
@Schema(name = "TokenResponseDto", description = "Пара токенов, выдаваемая при логине/обновлении")
public record TokenResponseDto(
    @Schema(description = "JWT токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,
    @Schema(description = "Время истечения JWT (UTC)", example = "2025-09-15T22:10:00Z")
    Instant accessTokenExpiresAt,
    @Schema(description = "Refresh-токен (UUID/строка), хранится на клиенте и используется для обновления",
        example = "6f5e8e0a-3d0a-4cda-b02d-9b1e3c6d89f4")
    String refreshToken
) {}
