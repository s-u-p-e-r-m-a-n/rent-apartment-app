package com.example.auth_module.service;

import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.model.RefreshToken;
import com.example.auth_module.model.UserEntity;

public interface RefreshTokenService {
    public TokenResponseDto rotate(String rawToken);
    public RefreshToken issue(UserEntity user);
}
