package com.example.auth_module.service.impl;

import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.model.RefreshToken;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.RefreshTokenRepository;
import com.example.auth_module.service.RefreshTokenService;
import com.example.auth_module.service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponseDto rotate(String rawToken) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(rawToken)
            .orElseThrow(() -> new UserException("Invalid refresh token",401));

        if (oldToken.isExpired() || oldToken.isRevoked()) {
            throw new UserException("Invalid or expired refresh token",401);
        }

        // revoke старый
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // новый refresh
        RefreshToken newToken = issue(oldToken.getUser());

        // новый access
        String access = jwtService.generateToken(oldToken.getUser().getLogin(), oldToken.getUser().getRoles());
        Instant accessExp = Instant.ofEpochMilli(jwtService.getExpiryEpochMillis(access));

        // единый ответ
        return new TokenResponseDto(access, accessExp, newToken.getToken());
    }

    @Value("${auth.refresh.ttl-days}")
    private long refreshTtlDays; // ← берём из настроек, дефолт 30
    @Transactional
    public RefreshToken issue(UserEntity user) {
        RefreshToken token = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiresAt(Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS))
            .revoked(false)
            .build();
        return refreshTokenRepository.save(token);
    }
}
