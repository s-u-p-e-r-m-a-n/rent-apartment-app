package com.example.auth_module.dto;


import java.time.Instant;
import java.util.List;

/**
 * Короткая инфа о текущем пользователе.
 */
public record MeResponseDto(
    String login,            // subject из JWT (login)
    List<String> roles,      // роли
    Instant expiresAt        // (опц.) срок действия access-токена, если сможем вытащить
) {}
