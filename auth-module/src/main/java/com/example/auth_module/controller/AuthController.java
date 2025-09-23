package com.example.auth_module.controller;

import com.example.auth_module.dto.MeResponseDto;
import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.dto.UserResponseDto;
import com.example.auth_module.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static com.example.auth_module.controller.AuthControllerPath.*;

@Log4j2
@Tag(name = "Auth", description = "Регистрация, авторизация и профиль")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Регистрация",
        description = "Создаёт пользователя (роль GUEST), отправляет 4-значный код верификации на e-mail"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "код отправлен"),

        // 400: неверные данные запроса (валидация)
        @ApiResponse(responseCode = "400",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "bad-request",
                    value = """
                        { "error": "Bad request parameters", "status": 400,
                          "path": "/api/auth/registration", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        // 409: конфликт — например, email уже занят
        @ApiResponse(responseCode = "409",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "conflict",
                    value = """
                        { "error": "Conflict: integrity constraint", "status": 409,
                          "path": "/api/auth/registration", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        // 422: бизнес-валидация (например, код подтверждения не прошёл)
        @ApiResponse(responseCode = "422",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "validation-failed",
                    value = """
                        { "error": "Validation failed", "status": 422,
                          "path": "/api/auth/registration", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        // 500  универсальный через components
        @ApiResponse(responseCode = "500", ref = "#/components/responses/ServerError")
    })
    @PostMapping(REGISTRATION_NEW_USER)
    public String registration(@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Данные для регистрации",
        required = true,
        content = @Content(
            schema = @Schema(implementation = UserRequestDto.class),
            examples = @ExampleObject(
                name = "Регистрация",
                value = """
                    {
                      "usernameValue": "Serega",
                      "loginValue": "user@example.com",
                      "passwordValue": "Password123!",
                      "code": null
                    }
                    """
            )
        )
    ) @Valid @RequestBody UserRequestDto userRequestDto) {
        return authService.registration(userRequestDto);
    }

    @Operation(
        summary = "Удаление пользователя (только SUPER_ADMIN)",
        description = "Удаляет пользователя по ID и возвращает обновлённый список пользователей",
        security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),

        @ApiResponse(responseCode = "400",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "bad-request",
                    value = """
                        { "error": "Bad request parameters", "status": 400,
                          "path": "/api/auth/admin/{id}", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "403",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "forbidden",
                    value = """
                        { "error": "Access denied", "status": 403,
                          "path": "/api/auth/admin/{id}", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "404",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "not-found",
                    value = """
                        { "error": "Resource not found", "status": 404,
                          "path": "/api/auth/admin/{id}", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "500", ref = "#/components/responses/ServerError")
    })

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(DELETE_USER)
    public List<UserResponseDto> delete(@Parameter(name = "id", description = "ID пользователя для удаления", required = true, example = "42")
                                        @PathVariable Long id) {
        return authService.deleteUser(id);
    }

    /**
     * Текущий пользователь по JWT.
     * Security:
     * - Должен быть залогинен (любой ролью).
     * - login берём из Authentication (заполнил JwtAuthFilter).
     * - roles берём из Authentication#getAuthorities (ROLE_* -> без префикса).
     * - (опц.) expiresAt берём из самого токена (из заголовка Authorization).
     */

    @Operation(
        summary = "Профиль (me)",
        description = "Возвращает сведения о текущем пользователе"
    )
    @SecurityRequirement(name = "bearerAuth") // <-- ОДИН раз
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),

        @ApiResponse(responseCode = "401",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "unauthorized",
                    value = """
                        { "error": "Authentication required or invalid token", "status": 401,
                          "path": "/api/auth/me", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "403",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "forbidden",
                    value = """
                        { "error": "Access denied", "status": 403,
                          "path": "/api/auth/me", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "500", ref = "#/components/responses/ServerError")
    })

    @PreAuthorize("isAuthenticated()")
    @GetMapping(GET_ME)
    public MeResponseDto me(Authentication auth, HttpServletRequest request) {
        // 1) login из Authentication (мы клали его как principal в JwtAuthFilter)
        String login = auth.getName();

        // 2) роли из Authentication (убираем префикс ROLE_)
        var roles = auth.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
            .toList();

        // 3) (опц.) получаем exp из запроса в атрибутах и кладем его для ответа
        Instant expiresAt = null;
        Object expAttr = request.getAttribute("jwt_exp");
        if (expAttr instanceof java.util.Date d) {
            expiresAt = d.toInstant();
        }

        return new MeResponseDto(login, roles, expiresAt);
    }

    @Operation(
        summary = "Авторизация",
        description = "Первый вход требует 4-значный код. Возвращает JWT."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),

        @ApiResponse(responseCode = "400",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "bad-request",
                    value = """
                        { "error": "Bad request parameters", "status": 400,
                          "path": "/api/auth/authorization", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "401",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "unauthorized",
                    value = """
                        { "error": "Authentication required or invalid token", "status": 401,
                          "path": "/api/auth/authorization", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "422",
            content = @Content(mediaType = "application/json",
                schema = @Schema(ref = "#/components/schemas/ApiError"),
                examples = @ExampleObject(
                    name = "validation-failed",
                    value = """
                        { "error": "Validation failed", "status": 422,
                          "path": "/api/auth/authorization", "timestamp": "2025-09-21T20:15:45Z" }
                        """
                ))),

        @ApiResponse(responseCode = "500", ref = "#/components/responses/ServerError")
    })

    @PostMapping(AUTHORIZATION_USER)
    public TokenResponseDto authorization(@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Данные для авторизации",
        required = true,
        content = @Content(
            schema = @Schema(implementation = UserRequestDto.class),
            examples = @ExampleObject(
                name = "Первый вход с кодом",
                value = """
                    {
                      "username": "Serega",
                      "email": "user@example.com",
                      "password": "Password123!",
                      "code": "1234"
                    }
                    """
            )
        )
    ) @Valid @RequestBody UserRequestDto userRequestDto) {
        return authService.authorization(userRequestDto);
    }

}
