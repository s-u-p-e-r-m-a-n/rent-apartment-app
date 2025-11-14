package com.example.auth_module.it;


import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;

import com.example.auth_module.model.RefreshToken;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.RefreshTokenRepository;
import com.example.auth_module.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshFlowIT extends BaseIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    UserRepository userRepository;

    String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";
    }

    // ======== HAPPY-PATH: refresh выдаёт новую пару, старый становится revoked ========
    @Test
    void refresh_ok_rotates_and_revokes_old() {
        // 1) Зарегистрировать и залогиниться → получить пару токенов
        register("u11", "u1@mail.com", "Password123!");
        UserEntity userEntity = userRepository.findByEmailJpql("u1@mail.com").orElseThrow();
        String code = userEntity.getVerification();
        TokenResponseDto pair1 = authorize("u11", "u1@mail.com", "Password123!", code);

        assertThat(pair1.accessToken()).isNotBlank();
        assertThat(pair1.refreshToken()).isNotBlank();

        // 2) Вызвать /refresh со старым refreshToken → получить новую пару
        TokenResponseDto pair2 = refresh(pair1.refreshToken());

        assertThat(pair2.accessToken()).isNotBlank();
        assertThat(pair2.refreshToken()).isNotBlank();
        assertThat(pair2.refreshToken()).isNotEqualTo(pair1.refreshToken());

        // 3) Проверить, что старый refresh помечен revoked=true
        RefreshToken old = refreshTokenRepository.findByToken(pair1.refreshToken()).orElseThrow();
        assertThat(old.isRevoked()).isTrue();
    }

    // ======== ПОВТОРНОЕ ИСПОЛЬЗОВАНИЕ СТАРОГО REFRESH ДОЛЖНО ДАВАТЬ 401 ========
    @Test
    void refresh_reuse_old_return_401() {
        register("u22", "u2@mail.com", "Password123!");
        UserEntity userEntity = userRepository.findByEmailJpql("u2@mail.com").orElseThrow();
        String code = userEntity.getVerification();
        TokenResponseDto pair1 = authorize("u22", "u2@mail.com", "Password123!", code);

        // первая ротация — валидна
        TokenResponseDto pair2 = refresh(pair1.refreshToken());
        assertThat(pair2.refreshToken()).isNotBlank();

        // повторная ротация со СТАРЫМ токеном → 401
        ResponseEntity<Map> resp = postJson(baseUrl + "/refresh", Map.of("refreshToken", pair1.refreshToken()), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // тело ошибки (ApiError)
        Map body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(401);
        assertThat(body.get("error")).isEqualTo("Invalid or expired refresh token");
        assertThat(String.valueOf(body.get("path"))).isEqualTo("/api/auth/refresh");
    }

    // ======== НЕИЗВЕСТНЫЙ REFRESH ДОЛЖЕН ДАВАТЬ 401 ========
    @Test
    void refresh_unknown_token_401() {
        ResponseEntity<Map> resp = postJson(baseUrl + "/refresh", Map.of("refreshToken", "unknown-123"), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ======== ПРОСРОЧЕННЫЙ REFRESH ДОЛЖЕН ДАВАТЬ 401 ========
    @Test
    void refresh_expired_401() {
        // Создадим пользователя через обычный поток и получим валидный refresh
        register("u33", "u3@mail.com", "Password123!");
        UserEntity userEntity = userRepository.findByEmailJpql("u3@mail.com").orElseThrow();
        String code = userEntity.getVerification();
        TokenResponseDto pair = authorize("u33", "u3@mail.com", "Password123!", code);

        // Насильно сделаем его просроченным (сдвинем expiresAt в прошлое)
        RefreshToken rt = refreshTokenRepository.findByToken(pair.refreshToken()).orElseThrow();
        rt.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        refreshTokenRepository.save(rt);

        // Теперь /refresh должен вернуть 401
        ResponseEntity<Map> resp = postJson(baseUrl + "/refresh", Map.of("refreshToken", pair.refreshToken()), Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ================= helpers =================

    private void register(String username, String email, String password) {
        // у твоего /registration возвращается строка — нам она не нужна, важно лишь, что 200 OK
        var req = new UserRequestDto(username, email, password, null);
        ResponseEntity<String> resp = postJson(baseUrl + "/registration", req, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private TokenResponseDto authorize(String username, String email, String password, String code) {
        var req = new UserRequestDto(username, email, password, code);
        ResponseEntity<TokenResponseDto> resp = postJson(baseUrl + "/authorization", req, TokenResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody();
    }

    private TokenResponseDto refresh(String refreshToken) {
        ResponseEntity<TokenResponseDto> resp = postJson(baseUrl + "/refresh",
            Map.of("refreshToken", refreshToken), TokenResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody();
    }

    private <T> ResponseEntity<T> postJson(String url, Object body, Class<T> type) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> http = new HttpEntity<>(body, h);
        return rest.postForEntity(url, http, type);
    }
}

