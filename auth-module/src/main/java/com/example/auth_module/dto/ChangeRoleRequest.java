package com.example.auth_module.dto;

/**
 * Тело запроса на смену роли.
 * Разрешаем только USER или ADMIN (валидация дополнительно в контроллере).
 */
public record ChangeRoleRequest(String role) {
}
