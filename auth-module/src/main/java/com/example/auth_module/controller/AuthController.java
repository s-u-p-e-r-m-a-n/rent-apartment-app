package com.example.auth_module.controller;

import com.example.auth_module.dto.*;
import com.example.auth_module.service.AuthService;
import com.example.auth_module.service.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
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
        @ApiResponse(responseCode = "200", description = "Код отправлен пользователю"),
        @ApiResponse(responseCode = "403", description = "Конфликт (дубликат/валидация)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Некорректный запрос",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
                          "username": "Serega",
                          "email": "user@example.com",
                          "password": "Password123!",
                          "code": null
                        }
                        """
            )
        )
    )@Valid @RequestBody UserRequestDto userRequestDto) {
        return authService.registration(userRequestDto);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(DELETE_USER)
    public List<UserResponseDto> delete(@PathVariable Long id) {
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
        description = "Возвращает сведения о текущем пользователе",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Успех"),
        @ApiResponse(responseCode = "401", description = "Невалидный/просроченный токен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Токен не передан",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
        @ApiResponse(responseCode = "200", description = "Успех",
            content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Неверный логин/пароль",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Неверный или отсутствующий код подтверждения",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Некорректный запрос",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
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
    )@Valid @RequestBody UserRequestDto userRequestDto) {
        return authService.authorization(userRequestDto);
    }

}
