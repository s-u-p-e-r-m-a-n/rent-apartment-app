package com.example.auth_module.unit.service;

import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.model.RefreshToken;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.RefreshTokenService;
import com.example.auth_module.service.ValidService;
import com.example.auth_module.service.impl.AuthServiceImpl;
import com.example.auth_module.service.security.JwtService;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceAuthorizationTest {

    @Mock
    private ValidService validService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;
    private UserRequestDto userRequestDto;
    private UserEntity userEntity;
    private static final String VERIFICATION_EQUALS = "verified";

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto("test@mail", "Serega", "123456", "1234");

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
        when(jwtService.generateToken(eq(userEntity.getLogin()), any()))
            .thenReturn("jwt-abc");
        when(jwtService.getExpiryEpochMillis("jwt-abc"))
            .thenReturn(123456789L);
        Instant rtExp = Instant.parse("2030-01-01T00:00:00Z");
        Instant rtCreated = Instant.parse("2030-01-01T00:00:00Z");
        when(refreshTokenService.issue(any(UserEntity.class)))
            .thenReturn(RefreshToken.builder()
                .user(userEntity)
                .token("ref-123")
                .expiresAt(rtExp)
                .createdAt(rtCreated)
                .revoked(false)
                .build());


        TokenResponseDto result = authService.authorization(userRequestDto);

        // Проверяем, что роль поднялась и верификация помечена
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertTrue(saved.getRoles().contains(UserEntity.Role.USER));
        assertEquals(VERIFICATION_EQUALS, saved.getVerification());
        verify(refreshTokenService).issue(eq(userEntity));
        verify(jwtService).generateToken(eq("test@mail"), any());


        assertNotNull(result);
        assertEquals("jwt-abc", result.accessToken());
        assertEquals(Instant.ofEpochMilli(123456789L), result.accessTokenExpiresAt());
        assertEquals("ref-123", result.refreshToken());
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
        assertEquals("Пользователь не существует", ex.getMessage()); // USER_DOES_NOT_EXIST
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
        UserRequestDto bad = new UserRequestDto("test@mail", "Serega", "123456", "123");


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

    @DisplayName("authorization: неверный пароль -> 401 WRONG_PASSWORD")
    @Test
    void authorization_wrongPassword_throws401() {
        // given
        UserRequestDto dto = new UserRequestDto("Serega", "test@mail", "123456", "123");
        UserEntity user = new UserEntity();
        user.setLogin("user@mail.com");
        user.setPasswordHash("encoded");
        user.setVerification("1234");
        user.setRoles(Set.of(UserEntity.Role.USER));

        when(userRepository.findByLoginCriteria(dto.loginValue()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.passwordValue(), "encoded"))
            .thenReturn(false); // пароль не совпал

        // when + then
        UserException ex = assertThrows(
            UserException.class,
            () -> authService.authorization(dto)
        );

        // проверяем статус/сообщение
        assertEquals(401, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Неверный пароль"));

        // валидация взаимодействий
        verify(userRepository).findByLoginCriteria(dto.loginValue());
        verify(passwordEncoder).matches(dto.passwordValue(), "encoded");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @DisplayName("Уже верифицирован (verification == ok)→200,без save")
    @Test
    void authorization_verifiedUser_ok_withoutCode() {
        UserRequestDto dto = new UserRequestDto("Serega", "test@mail.ru", "123456", "123");
        UserEntity user = new UserEntity();
        user.setLogin("test@mail.ru");
        user.setPasswordHash("123456");
        user.setVerification(VERIFICATION_EQUALS);
        user.setRoles(Set.of(UserEntity.Role.USER));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("token-123");

        when(userRepository.findByLoginCriteria(dto.loginValue()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.passwordValue(), "123456"))
            .thenReturn(true);
        when(jwtService.generateToken(eq(user.getLogin()), any())).thenReturn("jwt-abc");
        when(jwtService.getExpiryEpochMillis("jwt-abc")).thenReturn(1228982L);
        when(refreshTokenService.issue(any(UserEntity.class))).thenReturn(refreshToken);

        TokenResponseDto authorization = authService.authorization(dto);

        assertNotNull(authorization);
        assertEquals("jwt-abc", authorization.accessToken());
        assertEquals(1228982L, authorization.accessTokenExpiresAt().toEpochMilli());
        assertEquals("token-123", authorization.refreshToken());
        verify(userRepository).findByLoginCriteria(dto.loginValue());
        verify(passwordEncoder).matches(dto.passwordValue(), "123456");

        verify(jwtService).generateToken(eq(user.getLogin()), any());
        verify(jwtService).getExpiryEpochMillis("jwt-abc");
        verify(refreshTokenService).issue(any(UserEntity.class));
        verify(userRepository, never()).save(any());

    }

    @DisplayName("Код не передан для не верифицированного → 422")
    @Test
    void authorization_codeIsNull_throws422() {
        UserRequestDto dto = new UserRequestDto("Serega", "test@mail.ru", "123456", null);
        UserEntity user = new UserEntity();
        user.setLogin("test@mail.ru");
        user.setPasswordHash("123456");
        user.setVerification("1234");
        user.setRoles(Set.of(UserEntity.Role.USER));

        when(userRepository.findByLoginCriteria(dto.loginValue()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.passwordValue(), "123456"))
            .thenReturn(true);

        UserException userException = assertThrows(UserException.class, () -> authService.authorization(dto));
        assertEquals(422, userException.getErrorCode());
        assertEquals("Verification code is required", userException.getMessage());
        verify(userRepository).findByLoginCriteria(dto.loginValue());
        verify(passwordEncoder).matches(dto.passwordValue(), "123456");
        verifyNoInteractions(jwtService, refreshTokenService);
        verify(userRepository, never()).save(any());

    }


    @DisplayName("Неверифицированный + верный код → 200, с save()")
    @Test
    void authorization_unverified_withCorrectCode_ok_andPersistsVerification() {

        UserRequestDto dto = new UserRequestDto("Serega", "test@mail.ru", "123456", "1234");
        UserEntity user = new UserEntity();
        user.setLogin("test@mail.ru");
        user.setPasswordHash("123456");
        user.setVerification("1234");
        user.setRoles(Set.of(UserEntity.Role.USER));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("token-123");

        when(userRepository.findByLoginCriteria(dto.loginValue()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(dto.passwordValue(), "123456"))
            .thenReturn(true);
        when(jwtService.generateToken(eq(user.getLogin()), any())).thenReturn("jwt-abc");
        when(jwtService.getExpiryEpochMillis("jwt-abc")).thenReturn(1228982L);
        when(refreshTokenService.issue(any(UserEntity.class))).thenReturn(refreshToken);

        TokenResponseDto value = authService.authorization(dto);

        assertEquals("jwt-abc", value.accessToken());
        assertEquals(1228982L, value.accessTokenExpiresAt().toEpochMilli());
        assertEquals("token-123", value.refreshToken());
        var userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertEquals(VERIFICATION_EQUALS, saved.getVerification());
        assertEquals(UserEntity.Role.USER, Arrays.stream(saved.getRoles().stream().toArray()).toList().get(0));

    }


}
