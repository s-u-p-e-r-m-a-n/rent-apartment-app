package com.example.auth_module.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление access-токена")
public record RefreshRequest(
    @Schema(description = "Refresh-токен", example = "6f5e8e0a-3d0a-4cda-b02d-9b1e3c6d89f4")
    String refreshToken
) {}
