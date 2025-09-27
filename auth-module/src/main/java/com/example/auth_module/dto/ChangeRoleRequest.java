package com.example.auth_module.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Тело запроса на смену роли.
 * Разрешаем только USER или ADMIN (валидация дополнительно в контроллере).
 */
public record ChangeRoleRequest(
    @Schema(
        description = "Новая роль пользователя. Допустимые значения: USER, ADMIN",
        example = "ADMIN"
    )
    String role
)    {}
