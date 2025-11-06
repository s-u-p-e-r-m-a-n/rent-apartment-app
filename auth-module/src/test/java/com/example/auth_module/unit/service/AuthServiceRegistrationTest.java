package com.example.auth_module.unit.service;

import com.example.auth_module.dto.SenderDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.mapper.AuthMapper;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.EmailSenderIntegrationService;
import com.example.auth_module.service.ValidService;
import com.example.auth_module.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестируем метод регистрации пользователя")
public class AuthServiceRegistrationTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthMapper authMapper;
    @Mock
    private ValidService validService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailSenderIntegrationService emailSenderIntegrationService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRequestDto userRequestDto;

    @BeforeEach
    void setUp() {
        userRequestDto = userRequestDto = new UserRequestDto("Serega",
            "test@mail","123456","1234");


    }

    @DisplayName("Тестируем успешную регистрацию пользователя")
    @Test
    void checkingTheRegistrationSuccess() {
        when(validService.validation(anyString())).thenReturn(userRequestDto.loginValue());
        when(userRepository.findByEmailJpql(anyString())).thenReturn(Optional.empty());
        when(authMapper.mappUserDtoToUserEntity(any())).thenReturn(new UserEntity());
        when(passwordEncoder.encode(userRequestDto.passwordValue())).thenReturn("$hash123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(emailSenderIntegrationService.sendCodeVerification(any())).thenReturn("код отправлен");

        String result = authService.registration(userRequestDto);
        assertEquals("код отправлен", result);

        var userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertTrue(saved.getRoles().contains(UserEntity.Role.GUEST));
        assertNotNull(saved.getVerification());
        assertNotNull(saved.getPasswordHash());

        var senderCaptor = ArgumentCaptor.forClass(SenderDto.class);
        verify(emailSenderIntegrationService).sendCodeVerification(senderCaptor.capture());
        assertEquals("test@mail", senderCaptor.getValue().getEmail());
        assertNotNull(senderCaptor.getValue().getCode());

    }

    @DisplayName("Тестируем ошибку при некорректном e-mail")
    @Test
    void checkingTheRegistrationValidationError() {
        when(validService.validation(anyString())).thenThrow(new IllegalArgumentException("Invalid email format"));
        assertThrows(IllegalArgumentException.class, () -> authService.registration(userRequestDto));
        verifyNoInteractions(authMapper, userRepository, emailSenderIntegrationService);
    }

    @DisplayName("Ошибка при дублирующемся e-mail")
    @Test
    void registrationDuplicateEmail() {
        when(validService.validation(anyString())).thenReturn(userRequestDto.loginValue());
        when(userRepository.findByEmailJpql(userRequestDto.loginValue())).thenReturn(Optional.of(new UserEntity()));
        assertThrows(UserException.class, () -> authService.registration(userRequestDto));
        verifyNoInteractions(authMapper, emailSenderIntegrationService, passwordEncoder);

    }

    @DisplayName("Ошибка сохранения пользователя при регистрации")
    @Test
    void registrationSaveThrowsException() {
        when(validService.validation(anyString())).thenReturn(userRequestDto.loginValue());
        when(userRepository.findByEmailJpql(userRequestDto.loginValue())).thenReturn(Optional.empty());
        when(authMapper.mappUserDtoToUserEntity(any())).thenReturn(new UserEntity());
        when(passwordEncoder.encode(userRequestDto.passwordValue())).thenReturn("$hash123");
        when(userRepository.save(any(UserEntity.class))).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.registration(userRequestDto));
        assertEquals("DB error", ex.getMessage());
        verifyNoInteractions(emailSenderIntegrationService);

    }

    @DisplayName("Сбой отправки кода подтверждения")
    @Test
    void testAuthorizationIncorrectCodeValue() {
        when(validService.validation(anyString())).thenReturn(userRequestDto.loginValue());
        when(userRepository.findByEmailJpql(userRequestDto.loginValue())).thenReturn(Optional.empty());
        when(authMapper.mappUserDtoToUserEntity(any())).thenReturn(new UserEntity());
        when(passwordEncoder.encode(userRequestDto.passwordValue())).thenReturn("$hash123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailSenderIntegrationService.sendCodeVerification(any())).thenThrow(new UserException("ошибка код не отправлен", 666));

        assertThrows(UserException.class, () -> authService.registration(userRequestDto));
    }

    @DisplayName("registration: дубликат e-mail/login -> 409 (REGISTRATION_WITH_LOGIN_FAILED), без save и без отправки письма")
    @Test
    void registration_conflictLogin_throws409() {

        UserRequestDto dto = new UserRequestDto(
            "Serega",
            "test@mail.ru",
            "123456",
            null
        );

        when(validService.validation(dto.loginValue()))
            .thenReturn(dto.loginValue());
        when(userRepository.findByEmailJpql(dto.loginValue()))
            .thenReturn(Optional.of(new UserEntity()));


        UserException ex = assertThrows(
            UserException.class,
            () -> authService.registration(dto)
        );


        assertEquals(409, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Пользователь с таким логином существует"));

        verify(userRepository).findByEmailJpql(dto.loginValue());
        verify(userRepository, never()).save(any());
        verify(emailSenderIntegrationService, never()).sendCodeVerification(new SenderDto("test@mail.ru","3241"));
    }


}
