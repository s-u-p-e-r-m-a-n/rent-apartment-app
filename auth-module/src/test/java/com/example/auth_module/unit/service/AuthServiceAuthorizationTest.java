package com.example.auth_module.unit.service;

import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.impl.AuthServiceImpl;
import com.example.auth_module.service.security.JwtService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceAuthorizationTest {

    @Mock private ValidService validService;
    @Mock private UserRepository userRepository;
    @Mock private EntityManager entityManager;
    @Mock private EmailSenderIntegrationService emailSenderIntegrationService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRequestDto userRequestDto;
    private UserEntity userEntity;

    private static final String VERIFICATION_EQUALS = "verified";

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto("test@mail","Serega","123456","1234");

        userEntity = new UserEntity();
        userEntity.setLogin("test@mail");
        userEntity.setUsername("Serega");
        userEntity.setRoles(new HashSet<>(Set.of(UserEntity.Role.GUEST)));
        // теперь используем passwordHash вместо password
        userEntity.setPasswordHash("$hash123");
        userEntity.setVerification("1234");
    }

    @Test
    @DisplayName("Авторизация успешна для GUEST с верным кодом")
    void testAuthorizationGuestSuccess() {
        when(validService.validation(anyString())).thenReturn(userRequestDto.loginValue());
        when(userRepository.findByLoginCriteria(anyString()))
            .thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("123456", "$hash123")).thenReturn(true);
        when(jwtService.generateToken(eq("test@mail"), any()))
            .thenReturn("jwt-abc");
        when(jwtService.getExpiryEpochMillis("jwt-abc"))
            .thenReturn(123456789L);

        TokenResponseDto result = authService.authorization(userRequestDto);

        // Проверяем, что роль поднялась и верификация помечена
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertTrue(saved.getRoles().contains(UserEntity.Role.USER));
        assertEquals(VERIFICATION_EQUALS, saved.getVerification());


        assertNotNull(result);
        assertEquals("jwt-abc", result.accessToken());
        assertEquals(Instant.ofEpochMilli(123456789L), result.accessTokenExpiresAt());
    }

    @DisplayName("не корректный логин")
    @Test
    void testAuthorizationIncorrectLoginValue() {
        when(validService.validation(anyString())).thenReturn(userRequestDto.loginValue());
        // имитируем отсутствие пользователя
        when(userRepository.findByLoginCriteria(anyString()))
            .thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class,
            () -> authService.authorization(userRequestDto));
        assertEquals("Пользователь не существует", ex.getMessage()); // USER_DOёES_NOT_EXIST
        verify(userRepository, times(1)).findByLoginCriteria(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @DisplayName("не валидный e-mail")
    @Test
    void testAuthorizationIncorrectEmailValue() {
        when(validService.validation(anyString()))
            .thenThrow(new IllegalArgumentException("Invalid email format"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> authService.authorization(userRequestDto));
        assertEquals("Invalid email format", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    @DisplayName("Не подтвержденный email (неверный код)")
    @Test
    void testAuthorizationIncorrectCodeValue() {
        UserRequestDto bad = new UserRequestDto("test@mail","Serega","123456","123");


        when(validService.validation(anyString())).thenReturn(bad.loginValue());
        when(userRepository.findByLoginCriteria(anyString()))
            .thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("123456", "$hash123")).thenReturn(true);

        UserException ex = assertThrows(UserException.class,
            () -> authService.authorization(bad));
        assertEquals("Не подтвержденный email или неправильный код", ex.getMessage());
        // убедимся, что userRepository.save не вызывался (роль/верификация не менялись)
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
