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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестируем метод регистрации пользователя")
public class AuthServiceRegistrationMethodUnitTests {

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
    UserRequestDto userRequestDto = new UserRequestDto();

    @BeforeEach
    void setUp() {
        userRequestDto.setLoginValue("test@mail");
        userRequestDto.setUsernameValue("Serega");
        userRequestDto.setPasswordValue("123456");
        userRequestDto.setCode("1234");
    }

    @DisplayName("Тестируем успешную регистрацию пользователя ")
    @Test
    void checkingTheRegistrationSuccess() {

        /** Моки зависимостей*/
        when(validService.validation(anyString())).thenReturn(userRequestDto.getLoginValue());
        when(authMapper.mappUserDtoToUserEntity(any(UserRequestDto.class))).thenReturn(new UserEntity());
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());
        when(emailSenderIntegrationService.sendCodeVerification(any())).thenReturn("CODE_SENT");

        // Вызов тестируемого метода
        String result = authService.registration(userRequestDto);

        /**Проверки*/

        /**Проверяем, что отправлен e-mail с кодом*/

        assertEquals("CODE_SENT", result);
        verify(validService, times(1)).validation(anyString());
        verify(authMapper, times(1)).mappUserDtoToUserEntity(any(UserRequestDto.class));
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(emailSenderIntegrationService, times(1)).sendCodeVerification(any());

        /** Проверяем, что UserEntity сохраняется с ролью GUEST и кодом*/

        /** создаем захватчик аргументов*/
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        // проверяем, что метод sendEmail был вызван и ловим аргумент
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertTrue(savedUser.getRoles().contains(UserEntity.Role.GUEST));
        assertNotNull(savedUser.getVerification());

        /** Проверяем, что отправлен e-mail с кодом*/
        ArgumentCaptor<SenderDto> senderCaptor = ArgumentCaptor.forClass(SenderDto.class);
        verify(emailSenderIntegrationService).sendCodeVerification(senderCaptor.capture());
        SenderDto sender = senderCaptor.getValue();
        assertEquals(userRequestDto.getLoginValue(), sender.getEmail());
        assertNotNull(sender.getCode());
    }

    @DisplayName("Тестируем ошибку при некорректном e-mail")
    @Test
    void checkingTheRegistrationValidationError() {


        when(validService.validation(anyString())).thenThrow(new IllegalArgumentException("Invalid email format"));
        String result = authService.registration(userRequestDto);

        verify(validService, times(1)).validation(userRequestDto.getLoginValue());
        verifyNoInteractions(authMapper, userRepository); // другие сервисы не должны вызываться
    }

    @DisplayName("Ошибка при дублирующемся e-mail")
    @Test
    void registrationDuplicateEmail() {
        String loginValue = userRequestDto.getLoginValue();
        when(validService.validation(anyString())).thenReturn(loginValue);
        when(userRepository.findByEmailJpql(anyString())).thenReturn(Optional.of(new UserEntity()));
        //проверка что кидается ошибка
        assertThrows(UserException.class, () -> authService.registration(userRequestDto));

        /**проверяем, что метод валидации e-mail вызвался один раз с правильным аргументом.*/
        verify(validService).validation(loginValue);

        /**проверяем, что проверка существующего пользователя в базе была вызвана.*/
        verify(userRepository).findByEmailJpql(loginValue);
        verifyNoInteractions(authMapper, emailSenderIntegrationService);
    }
    @Test
    @DisplayName("Ошибка сохранения пользователя при регистрации")
    void registrationSaveThrowsException() {

        when(validService.validation(anyString())).thenReturn(userRequestDto.getLoginValue());
        when(authMapper.mappUserDtoToUserEntity(any(UserRequestDto.class))).thenReturn(new UserEntity());
        when(userRepository.save(any(UserEntity.class))).thenThrow(new RuntimeException("DB error"));

        // Проверяем, что исключение пробрасывается
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> authService.registration(userRequestDto));

        assertEquals("DB error", thrown.getMessage());

        // Проверяем, что до save() сервис дошёл
        verify(userRepository).save(any(UserEntity.class));
    }
    @DisplayName("сбой отправки кода подтверждения ")
    @Test
    void testAuthorizationIncorrectCodeValue(){

        when(validService.validation(anyString())).thenReturn(userRequestDto.getLoginValue());
        when(authMapper.mappUserDtoToUserEntity(any(UserRequestDto.class))).thenReturn(new UserEntity());
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());
        when(emailSenderIntegrationService.sendCodeVerification(any())).thenThrow(new UserException("ошибка код не отправлен", 666));

        assertThrows(UserException.class, () -> authService.registration(userRequestDto));
    }

}
