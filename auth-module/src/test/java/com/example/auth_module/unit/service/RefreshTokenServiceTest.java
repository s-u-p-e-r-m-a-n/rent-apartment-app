package com.example.auth_module.unit.service;

import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.model.RefreshToken;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.RefreshTokenRepository;
import com.example.auth_module.service.impl.RefreshTokenServiceImpl;
import com.example.auth_module.service.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    JwtService jwtService;
    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;


    @DisplayName("rotate: токен не найден -> 401, без генерации access-токена и без issue нового refresh")
    @Test
    void rotate_notFound_throws401() {

        String bad = "refresh-missing";
        when(refreshTokenRepository.findByToken(bad)).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class, () -> refreshTokenService.rotate(bad));
        assertEquals(401, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Invalid refresh token"));


        verify(refreshTokenRepository).findByToken(bad);
        verifyNoInteractions(jwtService);
        verify(refreshTokenRepository, never()).save(any());
    }

    @DisplayName("проверка просроченного токен, выбрасывает exception_401->Invalid or expired refresh token")
    @Test
    void expired_token_throws_401() {

        String bad = "refresh-missing";
        RefreshToken token = new RefreshToken();
        token.setExpiresAt(Instant.now().minusSeconds(1));
        token.setRevoked(false);
        token.setToken(bad);

        when(refreshTokenRepository.findByToken(bad)).thenReturn(Optional.of(token));

        UserException ex = assertThrows(UserException.class, () -> refreshTokenService.rotate(bad));
        assertEquals(401, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Invalid or expired refresh token"));
        verify(refreshTokenRepository).findByToken(bad);
        verifyNoInteractions(jwtService);
        verify(refreshTokenRepository, never()).save(any());


    }

    @DisplayName("rotate: успех — старый refresh revoked, новый сохранён и access сгенерирован")
    @Test
    void refresh_token_success_captors() {

        String oldToken = "refresh-old";
        String newAccessToken = "jwt-abc";

        UserEntity user = new UserEntity();
        user.setLogin("user@mail.ru");

        RefreshToken oldRefresh = new RefreshToken();
        oldRefresh.setToken(oldToken);
        oldRefresh.setUser(user);
        oldRefresh.setRevoked(false);
        oldRefresh.setExpiresAt(Instant.now().plusSeconds(3600)); // валидный

        when(refreshTokenRepository.findByToken(oldToken)).thenReturn(Optional.of(oldRefresh));
        when(jwtService.generateToken(eq(user.getLogin()), any())).thenReturn(newAccessToken);

        // пусть save(...) возвращает то, что ему передали (чтобы проверить поля)
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(inv -> inv.getArgument(0));


        TokenResponseDto result = refreshTokenService.rotate(oldToken);


        assertNotNull(result);
        assertEquals(newAccessToken, result.accessToken());
        assertNotNull(result.refreshToken());

        // захватываем оба сохранения: 1) старый -> revoked; 2) новый -> issued
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(tokenCaptor.capture());
        var savedTokens = tokenCaptor.getAllValues();


        // 1: старый токен должен быть помечен как revoked
        RefreshToken savedOld = savedTokens.get(0);
        assertEquals(oldToken, savedOld.getToken());
        assertTrue(savedOld.isRevoked());
        // 2: новый токен — не revoked, срок жизни в будущем, токен не пустой и не равен старому
        RefreshToken savedNew = savedTokens.get(1);
        assertFalse(savedNew.isRevoked());
        assertNotNull(savedNew.getToken());
        assertTrue(savedNew.getExpiresAt()!=null);
        assertNotEquals(oldToken, savedNew.getToken());
        assertSame(user, savedNew.getUser());
        // генерация access
        verify(jwtService).generateToken(eq(user.getLogin()), any());
        // поиск по старому refresh
        verify(refreshTokenRepository).findByToken(oldToken);
    }




}
