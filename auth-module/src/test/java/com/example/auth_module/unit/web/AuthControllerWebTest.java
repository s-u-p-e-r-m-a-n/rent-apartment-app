package com.example.auth_module.unit.web;


import com.example.auth_module.controller.AuthController;
import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.exception.UserExceptionHandler;
import com.example.auth_module.service.AuthService;
import com.example.auth_module.service.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(UserExceptionHandler.class)
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebTest {

    @Autowired
    MockMvc mvc;
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    AuthService authService;

    @Test
    void registration_200_plain_text() throws Exception {
        // given
        String body = """
            {
              "usernameValue": "Serega",
              "loginValue": "user@example.com",
              "passwordValue": "Password123!",
              "code": null
            }
            """;
        when(authService.registration(any())).thenReturn("код отправлен");

// when/then
        mvc.perform(post("/api/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(content().string("код отправлен"));

    }

    @Test
    void authorization_400_bad_body() throws Exception {
        mvc.perform(post("/api/auth/authorization")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/api/auth/authorization"));
        // verifyNoInteractions(authService); // сервис не должен вызываться
    }


    @Test
    void authorization_200_returns_tokens() throws Exception {
        // given
        String body = """
            {
              "usernameValue": "Serega",
              "loginValue": "user@example.com",
              "passwordValue": "Password123!",
              "code": "1234"
            }
            """;
        var now = Instant.parse("2025-09-21T20:15:45Z");
        when(authService.authorization(any()))
            .thenReturn(new TokenResponseDto("jwt-abc", now, "ref-123"));

// when/then
        mvc.perform(post("/api/auth/authorization")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value("jwt-abc"))
            .andExpect(jsonPath("$.accessTokenExpiresAt").value("2025-09-21T20:15:45Z"))
            .andExpect(jsonPath("$.refreshToken").value("ref-123"));

    }

    @Test
    void authorization_422_mapped_by_UserException() throws Exception {
        // given
        String body = """
            {
              "usernameValue": "Serega",
              "loginValue": "user@example.com",
              "passwordValue": "Password123!",
              "code": "0000"
            }
            """;
        when(authService.authorization(any()))
            .thenThrow(new UserException("Validation failed", 422));

// when/then
        mvc.perform(post("/api/auth/authorization")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnprocessableEntity()) // 422
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.path").value("/api/auth/authorization"));

    }

}
