package com.example.auth_module.unit.web;

import com.example.auth_module.controller.RefreshController;
import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.service.RefreshTokenService;
import com.example.auth_module.service.security.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RefreshController.class)
@AutoConfigureMockMvc
class RefreshControllerWebTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            // В тесте разрешаем доступ, чтобы проверить поведение контроллера
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/refresh").permitAll()
                    .anyRequest().permitAll()
                );
            return http.build();
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void passThroughJwtFilter() throws Exception {
        doAnswer(inv -> {
            HttpServletRequest req = inv.getArgument(0);
            HttpServletResponse res = inv.getArgument(1);
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    }

    @Test
    @DisplayName("POST /api/auth/refresh -> 200 OK (валидный refresh)")
    void refresh_ok() throws Exception {
        String refresh = "good-refresh";

        TokenResponseDto dto = new TokenResponseDto(
            "new-access",
            Instant.now().plusSeconds(900),
            "new-refresh"
        );

        BDDMockito.given(refreshTokenService.rotate(refresh)).willReturn(dto);

        mvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh + "\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value("new-access"))
            .andExpect(jsonPath("$.accessTokenExpiresAt").exists())
            .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh -> 401 Unauthorized (просроченный/битый refresh)")
    void refresh_unauthorized_badToken() throws Exception {
        String bad = "expired-or-bad";

        BDDMockito.given(refreshTokenService.rotate(bad))
            .willThrow(new UserException(
                UserException.AUTHORIZATION_TOKEN_FAILED,
                UserException.AUTHORIZATION_TOKEN_FAILED_CODE
            ));

        mvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + bad + "\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/refresh -> 400 Bad Request (пустое/битое тело)")
    void refresh_badRequest_noBody() throws Exception {
        mvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
