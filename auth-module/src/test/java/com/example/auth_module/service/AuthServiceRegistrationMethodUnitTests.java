package com.example.auth_module.service;

import com.example.auth_module.dto.SenderDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.mapper.AuthMapper;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестируем метод регистрации пользователя")
public class AuthServiceRegistrationMethodUnitTests {

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
        userRequestDto = userRequestDto = new UserRequestDto("test@mail",
            "Serega","123456","1234");


    }

    @DisplayName("Тестируем успешную регистрацию пользователя")
    @Test
    void checkingTheRegistrationSuccess() {
        when(validService.validation("test@mail")).thenReturn("test@mail");
        when(userRepository.findByEmailJpql("test@mail")).thenReturn(Optional.empty());
        when(authMapper.mappUserDtoToUserEntity(any())).thenReturn(new UserEntity());
        when(passwordEncoder.encode("123456")).thenReturn("$hash123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(emailSenderIntegrationService.sendCodeVerification(any())).thenReturn("CODE_SENT");

        String result = authService.registration(userRequestDto);
        assertEquals("CODE_SENT", result);

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
        when(validService.validation("test@mail")).thenThrow(new IllegalArgumentException("Invalid email format"));
        assertThrows(IllegalArgumentException.class, () -> authService.registration(userRequestDto));
        verifyNoInteractions(authMapper, userRepository, emailSenderIntegrationService);
    }

    @DisplayName("Ошибка при дублирующемся e-mail")
    @Test
    void registrationDuplicateEmail() {
        when(validService.validation("test@mail")).thenReturn("test@mail");
        when(userRepository.findByEmailJpql("test@mail")).thenReturn(Optional.of(new UserEntity()));
        assertThrows(UserException.class, () -> authService.registration(userRequestDto));
        verifyNoInteractions(authMapper, emailSenderIntegrationService, passwordEncoder);

    }

    @DisplayName("Ошибка сохранения пользователя при регистрации")
    @Test
    void registrationSaveThrowsException() {
        when(validService.validation("test@mail")).thenReturn("test@mail");
        when(userRepository.findByEmailJpql("test@mail")).thenReturn(Optional.empty());
        when(authMapper.mappUserDtoToUserEntity(any())).thenReturn(new UserEntity());
        when(passwordEncoder.encode("123456")).thenReturn("$hash123");
        when(userRepository.save(any(UserEntity.class))).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.registration(userRequestDto));
        assertEquals("DB error", ex.getMessage());
        verifyNoInteractions(emailSenderIntegrationService);

    }

    @DisplayName("Сбой отправки кода подтверждения")
    @Test
    void testAuthorizationIncorrectCodeValue() {
        when(validService.validation("test@mail")).thenReturn("test@mail");
        when(userRepository.findByEmailJpql("test@mail")).thenReturn(Optional.empty());
        when(authMapper.mappUserDtoToUserEntity(any())).thenReturn(new UserEntity());
        when(passwordEncoder.encode("123456")).thenReturn("$hash123");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailSenderIntegrationService.sendCodeVerification(any())).thenThrow(new UserException("ошибка код не отправлен", 666));

        assertThrows(UserException.class, () -> authService.registration(userRequestDto));

    }
}
