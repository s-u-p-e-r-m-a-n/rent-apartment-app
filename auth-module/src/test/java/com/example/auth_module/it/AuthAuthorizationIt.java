package com.example.auth_module.it;


import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import com.example.auth_module.service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthAuthorizationIt extends BaseIT {

    @Autowired
    AuthService authService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TestRestTemplate rest;
    @Autowired
    JwtService jwtService;

    private String username = "Serega";
    private String email = "it_authz@example.com";
    private String pwd = "Password123!";
    private static final String AUTHZ_URL = "/api/auth/authorization";

    @Test
    @DisplayName("POST /api/auth/authorization: выдаёт JWT (первый вход с кодом)")
    void authorization_returns_jwt_on_first_login_with_code() {

        authService.registration(new UserRequestDto(username, email, pwd, null));
        UserEntity savedLogin = userRepository.findByEmailJpql(email).orElseThrow();
        String code = savedLogin.getVerification();
        //Логинимся через REST (authorization) с правильным кодом
        var reqDto = new UserRequestDto(username, email, pwd, code);
        ResponseEntity<TokenResponseDto> resp =
            rest.postForEntity("/api/auth/authorization", reqDto, TokenResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        var body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.accessToken()).isNotBlank();

        //Лёгкая проверка токена: формат + payload
        String token = body.accessToken();
        assertThat(token).startsWith("eyJ"); // JWT header base64
        assertThat(jwtService.getLogin(token)).isEqualToIgnoringCase(email);
        assertThat(jwtService.getRoles(token)).contains(UserEntity.Role.USER.name()); // после верификации роль USER

        long expMs = jwtService.getExpiryEpochMillis(token);
        assertThat(expMs).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    @DisplayName("POST /api/auth/authorization: повторный вход (без смены кода) тоже возвращает JWT")
    void authorization_returns_jwt_on_repeated_login() {
        // Подготовка: первый вход с кодом
        authService.registration(new UserRequestDto(username, email, pwd, null));
        String code = userRepository.findByEmailJpql(email).orElseThrow().getVerification();

        var firstReq = new UserRequestDto(username, email, pwd, code);
        var first = rest.postForEntity("/api/auth/authorization", firstReq, TokenResponseDto.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Повторный вход: пользователь уже верифицирован, код всё равно обязателен @Valid — подадим тот же
        var secondReq = new UserRequestDto(username, email, pwd, code);
        var second = rest.postForEntity("/api/auth/authorization", secondReq, TokenResponseDto.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getBody()).isNotNull();
        assertThat(second.getBody().accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("authorization: пустой verification-код → 400 BAD_REQUEST (валидация @Valid)")
    void authorization_empty_code_returns_400() {
        authService.registration(new UserRequestDto(username, email, pwd, null));

        // Пустой код — нарушает @NotBlank (если стоит на поле code)
        var req = new UserRequestDto(username, email, pwd, "");
        ResponseEntity<String> resp = rest.postForEntity(AUTHZ_URL, req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    @Test
    @DisplayName("authorization: неверный пароль ловим ошибку 401")
    void authorization_returns_401() {
        authService.registration(new UserRequestDto(username, email, pwd, null));
        UserEntity userEntity = userRepository.findByEmailJpql(email).orElseThrow();
        String code = userEntity.getVerification();
        var badReq = new UserRequestDto(username, email, "WrongPass123!", code);
        ResponseEntity<String> resp = rest.postForEntity(AUTHZ_URL, badReq, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


}
