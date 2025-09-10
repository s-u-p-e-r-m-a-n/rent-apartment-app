package com.example.auth_module.dto;

import com.example.auth_module.model.UserEntity;

/**
 * Короткий ответ о пользователе после смены роли.
 */
public record UserShortDto(Long id, String login, String username, String role) {

    public static UserShortDto from(UserEntity u) {
        // В модели roles — Set<Role>. Держим ОДНУ активную роль.
        String roleName = u.getRoles() != null && !u.getRoles().isEmpty()
            ? u.getRoles().iterator().next().name()
            : "GUEST"; // страховка
        return new UserShortDto(
            u.getId(),
            u.getLogin(),
            u.getUsername(),
            roleName
        );
    }

}
