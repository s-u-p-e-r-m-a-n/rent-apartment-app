package com.example.auth_module.dto;


import java.time.Instant;

/**
 * Ответ со сгенерированным JWT токеном доступа.
 */
public record TokenResponseDto(
    String accessToken,
    Instant accessTokenExpiresAt
) {}