package com.example.auth_module.controller.admin;

import com.example.auth_module.dto.ChangeRoleRequest;
import com.example.auth_module.dto.UserShortDto;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.service.admin.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.GrantedAuthority;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class UserAdminController {


    private final UserAdminService userAdminService;

    /**
     * Смена роли у пользователя (USER <-> ADMIN).
     * Доступ: ADMIN и SUPER_ADMIN.
     *
     * @param id   targetId — у кого меняем (из URL)
     * @param body содержит желаемую роль (USER или ADMIN)
     * @param auth Authentication с актором (кто вызвал) — приходит из JWT
     */

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/{id}/role")
    public UserShortDto changeRole(@PathVariable("id") Long id,
                                   @RequestBody ChangeRoleRequest body,
                                   Authentication auth) {
        // --- 1) Достаём данные актёра (кто делает запрос) из SecurityContext ---
        // name() указывает на principal name — в конфигурации это login (subject из JWT).
        String actorLogin = auth.getName();
        // Множество авторитетов вида: ROLE_ADMIN, ROLE_SUPER_ADMIN и т.п.
        Set<String> actorAuthorities = auth.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
        // --- 2) Валидируем запрошенную роль ---
        // В проекте роль — это вложенный enum: UserEntity.Role (GUEST, USER, ADMIN, SUPER_ADMIN).
        UserEntity.Role targetRole;
        try {
            targetRole = UserEntity.Role.valueOf(body.role().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Некорректная роль. Разрешено: USER или ADMIN."
            );
        }

        // Ограничиваем публичный API: нельзя назначать GUEST/SUPER_ADMIN
        if (targetRole == UserEntity.Role.GUEST || targetRole == UserEntity.Role.SUPER_ADMIN) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Вы не можете назначать только USER или ADMIN."
            );
        }

        // --- 3) Делегируем в сервис бизнес-логику ---
        // Сервис сам проверит правила:
        // - Нельзя менять себя
        // - ADMIN может только повышать USER->ADMIN
        // - Понижать ADMIN->USER может только SUPER_ADMIN
        // - SUPER_ADMIN менять нельзя
        return userAdminService.changeRole(id, targetRole, actorLogin, actorAuthorities);
    }

}
