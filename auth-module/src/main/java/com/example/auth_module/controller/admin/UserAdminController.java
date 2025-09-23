package com.example.auth_module.controller.admin;

import com.example.auth_module.dto.ChangeRoleRequest;
import com.example.auth_module.dto.UserShortDto;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.service.admin.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    @Operation(summary = "Изменить роль пользователя по идентификатору")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),

        @ApiResponse(responseCode = "400",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "bad-request",
                    value = """
                        { "error": "Bad request parameters", "status": 400,
                          "path": "/api/auth/admin/{id}/role", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "403",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "forbidden",
                    value = """
                        { "error": "Access denied", "status": 403,
                          "path": "/api/auth/admin/{id}/role", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "404",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "not-found",
                    value = """
                        { "error": "Resource not found", "status": 404,
                          "path": "/api/auth/admin/{id}/role", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "409",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "conflict",
                    value = """
                        { "error": "Conflict: integrity constraint", "status": 409,
                          "path": "/api/auth/admin/{id}/role", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "500", ref = "#/components/responses/ServerError")
    })

// @PatchMapping("/{id}/role") — маппинг оставляем как у тебя

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
        // --- 2) Валидируем запрошенную роль ---x`
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
