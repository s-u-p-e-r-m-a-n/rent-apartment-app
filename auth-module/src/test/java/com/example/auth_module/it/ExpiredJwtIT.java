package com.example.auth_module.it;

import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import com.example.auth_module.service.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpiredJwtIT extends BaseIT {

    private static final String ME = "/api/auth/me";

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired TestRestTemplate rest;
    @Autowired JwtService jwtService;

    @Test
    @DisplayName("expired JWT: /api/auth/me -> 403 (токен с exp в прошлом)")
    void expired_token_is_rejected_immediately() {
        // 1) Готовим пользователя (чтобы subject/login был реальным email)
        String username = "Serega";
        String email    = "it_expired@example.com";
        String pwd      = "Password123!";

        authService.registration(new UserRequestDto(username, email, pwd, null));
        // валидация не нужна — токен генерим вручную

        // 2) Делаем TTL отрицательным, чтобы exp был в прошлом
        Long originalTtl = (Long) ReflectionTestUtils.getField(jwtService, "ttlMin");
        ReflectionTestUtils.setField(jwtService, "ttlMin", -1L);
        try {
            // 3) Генерим токен сразу «просроченным» (роль USER, чтобы пройти авторизацию, если она проверяется)
            String jwt = jwtService.generateToken(email, List.of(UserEntity.Role.USER));
            assertThat(jwt).isNotBlank();

            // 4) Идём на защищённый эндпоинт → ожидаем 403 (у тебя так для expired/invalid)
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(jwt);
            ResponseEntity<String> resp = rest.exchange(ME, HttpMethod.GET, new HttpEntity<>(h), String.class);

            assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        } finally {
            // 5) Возвращаем исходный TTL, чтобы не влиять на другие тесты
            if (originalTtl != null) {
                ReflectionTestUtils.setField(jwtService, "ttlMin", originalTtl);
            }
        }
    }
}
