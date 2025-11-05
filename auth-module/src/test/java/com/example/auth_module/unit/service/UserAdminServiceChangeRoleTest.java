package com.example.auth_module.unit.service;

import com.example.auth_module.dto.UserShortDto;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.admin.impl.UserAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@DisplayName("Тестируем метод изменения роли пользователя")
@ExtendWith(MockitoExtension.class)
public class UserAdminServiceChangeRoleTest {


    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private UserAdminServiceImpl userAdminService;

    @BeforeEach
    void setUp() {

    }

    @DisplayName("метод проходит без ошибок ->OK ")
    @Test
    void changeRole_ok() {
        Long userId = 1L;
        UserEntity.Role role = UserEntity.Role.ADMIN;
        String actorLogin = "admin@mail.ru";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setLogin("test@mail");
        userEntity.setUsername("Serega");
        userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.USER)));
        Set<String> actorAuthorities = Set.of("ROLE_ADMIN");


        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0)); // возвращает ту же сущность
        UserShortDto result = userAdminService.changeRole(userId, role, actorLogin, actorAuthorities);

        assertNotNull(result);
        assertEquals(UserShortDto.class, result.getClass());
        assertEquals("ADMIN", result.role());
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertTrue(saved.getRoles().contains(role));
        assertEquals("test@mail", saved.getLogin());
        assertEquals("Serega", saved.getUsername());
        assertEquals(userId, saved.getId());

        verify(userRepository).findById(userId);
        verify(userRepository).save(userEntity);
        verifyNoMoreInteractions(userRepository);

    }

    @DisplayName("метод не проходит бросается исключение NOT_FOUND 404 ")
    @Test
    void changeRole_notFound_throws404() {
        Long userId = 1L;
        UserEntity.Role role = UserEntity.Role.ADMIN;
        String actorLogin = "admin@mail.ru";
        Set<String> actorAuthorities = Set.of("ROLE_ADMIN");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> {
                userAdminService.changeRole(userId, role, actorLogin, actorAuthorities);
            });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Пользователь не найден", ex.getReason());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoMoreInteractions(userRepository);

    }

    @DisplayName("метод не проходит бросается исключение NOT_FOUND 400 ")
    @Test
    void changeRole_invalidRole_throws400() {

        Long userId = 1L;
        UserEntity.Role role = UserEntity.Role.GUEST;
        String actorLogin = "admin@mail.ru";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setLogin("test@mail");
        userEntity.setUsername("Serega");
        userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.USER)));
        Set<String> actorAuthorities = Set.of("ROLE_SUPER_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> {
                userAdminService.changeRole(userId, role, actorLogin, actorAuthorities);
            });
        assertEquals("Разрешено только USER или ADMIN", ex.getReason());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);


    }

    @DisplayName("метод не проходит бросается исключение FORBIDDEN 403 ")
    @Test
    void forbidden_throws403() {
        Long userId = 1L;
        UserEntity.Role role = UserEntity.Role.GUEST;
        String actorLogin = "admin@mail.ru";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setLogin("test@mail");
        userEntity.setUsername("Serega");
        userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.USER)));
        Set<String> actorAuthorities = Set.of("ROLE_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> {
                userAdminService.changeRole(userId, role, actorLogin, actorAuthorities);
            });
        assertEquals("ADMIN может только повышать USER -> ADMIN", ex.getReason());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @DisplayName("метод не проходит бросается исключение FORBIDDEN 403 Нельзя изменять роль SUPER_ADMIN")
    @Test
    void forbidden_throws403_forbidden_role_chang_super_admin() {
        Long userId = 1L;
        UserEntity.Role role = UserEntity.Role.ADMIN;
        String actorLogin = "admin@mail.ru";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setLogin("test@mail");
        userEntity.setUsername("Serega");
        userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.SUPER_ADMIN)));
        Set<String> actorAuthorities = Set.of("ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> {
                userAdminService.changeRole(userId, role, actorLogin, actorAuthorities);
            });
        assertEquals("Нельзя изменять роль SUPER_ADMIN", ex.getReason());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }
    @DisplayName("метод не проходит бросается исключение FORBIDDEN 403 Нельзя менять собственную роль")
    @Test
    void forbidden_throws403_forbidden_role_chang_own_role() {
        Long userId = 1L;
        UserEntity.Role role = UserEntity.Role.ADMIN;
        String actorLogin = "admin@mail.ru";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setLogin("admin@mail.ru");
        userEntity.setUsername("Serega");
        userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.SUPER_ADMIN)));
        Set<String> actorAuthorities = Set.of("SUPER_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> {
                userAdminService.changeRole(userId, role, actorLogin, actorAuthorities);
            });
        assertEquals("Нельзя менять собственную роль", ex.getReason());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }


}
