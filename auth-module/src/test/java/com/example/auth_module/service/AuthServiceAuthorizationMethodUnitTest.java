package com.example.auth_module.service;

import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.impl.AuthServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceAuthorizationMethodUnitTest {

    @Mock
    private ValidService validService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private EmailSenderIntegrationService emailSenderIntegrationService;
    @InjectMocks
    private AuthServiceImpl authService;

    private UserRequestDto userRequestDto;
    private UserEntity userEntity;


    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setLoginValue("test@mail");
        userRequestDto.setUsernameValue("Serega");
        userRequestDto.setPasswordValue("123456");
        userRequestDto.setCode("1234");

        userEntity = new UserEntity();
        userEntity.setLogin("test@mail");
        userEntity.setUsername("Serega");
        userEntity.setRoles(Set.of(UserEntity.Role.GUEST));
        userEntity.setPassword(Base64EncodDecod.encode("123456"));
        userEntity.setVerification("1234");

    }

    @Test
    @DisplayName("Авторизация успешна для GUEST с верным кодом")
    void testAuthorizationGuestSuccess() {

        /** Моки зависимостей*/
        when(userRepository.findByLoginCriteria(userRequestDto.getLoginValue())).thenReturn(Optional.of(userEntity));
        when(validService.validation(anyString())).thenReturn(userRequestDto.getLoginValue());
        // Вызов тестируемого метода
        String result = authService.authorization(userRequestDto);

        /**Проверки*/

        /**Проверяем, смену роли на USER и генерацию токена */
        assertTrue(result.contains("вы успешно авторизировались"));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());// ловим сохранённого пользователя
        UserEntity saveUser = userCaptor.getValue();
        assertTrue(saveUser.getRoles().contains(UserEntity.Role.USER));
        assertNotNull(saveUser.getPassword());
        assertNotNull(saveUser.getToken());

    }

    @DisplayName("не корректный логин")
    @Test
    void testAuthorizationIncorrectLoginValue() {

        when(userRepository.findByLoginCriteria(userRequestDto.getLoginValue()))
                .thenThrow(new UserException("Пользователь не существует", 705));
        when(validService.validation(anyString())).thenReturn(userRequestDto.getLoginValue());

        UserException userException = assertThrows(UserException.class, () -> authService.authorization(userRequestDto));
        assertEquals("Пользователь не существует", userException.getMessage());
        verifyNoMoreInteractions(userRepository);

    }

    @DisplayName("не валидный e-mail")
    @Test
    void testAuthorizationIncorrectEmailValue() {

        when(validService.validation(anyString())).thenThrow(new IllegalArgumentException("Invalid email format"));
        String result = authService.authorization(userRequestDto);
        assertEquals("Invalid email format", result);
    }

    @DisplayName("Не подтвержденный email ")
    @Test
    void testAuthorizationIncorrectCodeValue() {

        UserRequestDto userIncorrectInf = new UserRequestDto();
        userIncorrectInf.setLoginValue("test@mail");
        userIncorrectInf.setUsernameValue("Serega");
        userIncorrectInf.setPasswordValue("123456");
        userIncorrectInf.setCode("123");

        when(userRepository.findByLoginCriteria(userIncorrectInf.getLoginValue())).thenReturn(Optional.of(userEntity));
        when(validService.validation(anyString())).thenReturn(userIncorrectInf.getLoginValue());

        String result = authService.authorization(userIncorrectInf);
        assertEquals("Не подтвержденный email или неправильный код", result);

    }

}
