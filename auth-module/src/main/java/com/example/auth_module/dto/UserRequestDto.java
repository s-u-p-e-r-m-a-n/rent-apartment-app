package com.example.auth_module.dto;


import jakarta.validation.constraints.*;

public record UserRequestDto(
    @NotBlank
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Допустимы буквы, цифры, . _ -")
    String usernameValue,

    @NotBlank
    @Email
    @Size(max = 128)
    String loginValue,          // email

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
        message = "Пароль: строчная/ЗАГЛАВНАЯ/цифра/спецсимвол"
    )
    String passwordValue,

    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "Код из 4 цифр")
    String code
) {}

