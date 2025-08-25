package com.example.auth_module.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto {

    /**
     * Запрос на логин.
     * email -> ищем в user_info.login
     * password -> сверяем с user_info.password_hash через PasswordEncoder.matches
     */
    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

}
