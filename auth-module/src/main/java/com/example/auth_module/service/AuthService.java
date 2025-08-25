package com.example.auth_module.service;

import com.example.auth_module.dto.TokenResponseDto;
import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.dto.UserResponseDto;

import java.util.List;

public interface AuthService {

    public String registration(UserRequestDto userRequestDto);

    public TokenResponseDto authorization(UserRequestDto userRequestDto);

    public String checkingUserRole(UserRequestDto userRequestDto);
    public String changeRole(Long id);
    public List<UserResponseDto> deleteUser(Long id);
}
