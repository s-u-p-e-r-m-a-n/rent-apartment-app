package com.example.auth_module.unit.web;

import com.example.auth_module.config.SecurityConfig;
import com.example.auth_module.controller.admin.UserAdminController;
import com.example.auth_module.dto.UserShortDto;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.service.admin.UserAdminService;
import com.example.auth_module.service.security.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserAdminController.class)
@AutoConfigureMockMvc // фильтры включены
@Import(SecurityConfig.class)
class UserAdminControllerWebTest {

    @Autowired
    private MockMvc mvc;

    // мок фильтра, чтобы SecurityConfig собрался без реального JwtService
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserAdminService userAdminService;

    @BeforeEach
    void setupJwtFilter() throws Exception {
        // ОБЯЗАТЕЛЬНО: прокинуть цепочку, иначе контроллер не вызовется
        doAnswer(inv -> {
            HttpServletRequest req = inv.getArgument(0);
            HttpServletResponse res = inv.getArgument(1);
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    @DisplayName("PATCH /api/auth/admin/{id}/role -> 200 OK + UserShortDto")
    void changeRole_ok() throws Exception {
        long id = 5L;
        var dto = new UserShortDto(id, "user@mail.com", "Vasya", "ADMIN");

        given(userAdminService.changeRole(
            eq(id),
            eq(UserEntity.Role.ADMIN),
            eq("admin@mail.com"),
            anySet()
        )).willReturn(dto);

        mvc.perform(patch("/api/auth/admin/{id}/role", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value((int) id))
            .andExpect(jsonPath("$.login").value("user@mail.com"))
            .andExpect(jsonPath("$.username").value("Vasya"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    @DisplayName("PATCH /api/auth/admin/{id}/role -> 404 Not Found (прокидываем исключение сервиса)")
    void changeRole_notFound_fromService() throws Exception {
        long missingId = 999L;

        given(userAdminService.changeRole(
            eq(missingId),
            eq(UserEntity.Role.ADMIN),
            eq("admin@mail.com"),
            anySet()
        )).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mvc.perform(patch("/api/auth/admin/{id}/role", missingId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    @DisplayName("PATCH /api/auth/admin/{id}/role с некорректной ролью -> 400 Bad Request (валидация контроллера)")
    void changeRole_badRequest_whenInvalidRole() throws Exception {
        mvc.perform(patch("/api/auth/admin/{id}/role", 10L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"GUEST\"}")) // контроллер разрешает только USER/ADMIN
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/auth/admin/{id}/role -> 403 Forbidden для USER без прав ADMIN/SUPER_ADMIN")
    @WithMockUser(username = "user@mail.com", roles = {"USER"})
    void changeRole_forbidden_whenNotAdmin() throws Exception {
        mvc.perform(patch("/api/auth/admin/{id}/role", 15L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
            .andExpect(status().isForbidden());

        // Сервис не должен вызываться при 403
        verifyNoInteractions(userAdminService);
    }

    @Test
    @DisplayName("PATCH /api/auth/admin/{id}/role -> 400 Bad Request при пустом/битом JSON")
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void changeRole_badRequest_whenEmptyBody() throws Exception {
        mvc.perform(patch("/api/auth/admin/{id}/role", 10L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // нет поля role
            .andExpect(status().isBadRequest());

        // Сервис не должен вызываться при 400
        verifyNoInteractions(userAdminService);
    }
}
