package com.example.auth_module.controller;

import com.example.auth_module.dto.MeResponseDto;
import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.dto.UserResponseDto;
import com.example.auth_module.mapper.AuthMapper;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import com.example.auth_module.service.security.JwtService;
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
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping(REGISTRATION_NEW_USER)
    public String registration(@Valid @RequestBody UserRequestDto userRequestDto) {
        return authService.registration(userRequestDto);
    }
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping(DELETE_USER)
    public List<UserResponseDto> delete(@PathVariable Long id) {
        return authService.deleteUser(id);
    }

    /**
     * Текущий пользователь по JWT.
     * Security:
     *  - Должен быть залогинен (любой ролью).
     *  - login берём из Authentication (заполнил JwtAuthFilter).
     *  - roles берём из Authentication#getAuthorities (ROLE_* -> без префикса).
     *  - (опц.) expiresAt берём из самого токена (из заголовка Authorization).
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping(GET_ME)
    public MeResponseDto me(Authentication auth, HttpServletRequest request, JwtService jwtService) {
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
    @PostMapping(AUTHORIZATION_USER)
    public TokenResponseDto authorization(@Valid @RequestBody UserRequestDto userRequestDto) {
        return authService.authorization(userRequestDto);
    }

    @GetMapping(ADD_COMMENT)
    public String addComment(@RequestParam String comment, @RequestHeader String token) {

        return null;
    }
}
