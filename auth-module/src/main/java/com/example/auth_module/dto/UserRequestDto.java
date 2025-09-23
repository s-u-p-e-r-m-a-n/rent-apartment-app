package com.example.auth_module.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
    @NotBlank
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Допустимы буквы, цифры, . _ -")
    @Schema(description = "Имя пользователя (login/username)", example = "Serega")
    String usernameValue,

    @NotBlank
    @Email
    @Size(max = 128)
    @Schema(description = "E-mail (используется как логин)", example = "user@example.com")
    String loginValue,          // email

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
        message = "Пароль: строчная/ЗАГЛАВНАЯ/цифра/спецсимвол"
    )
    @Schema(description = "Пароль", example = "Password123!")
    String passwordValue,


    @Pattern(regexp = "^\\d{4}$", message = "Код из 4 цифр")
    @Schema(description = "4-значный код подтверждения из письма (для первого входа)", example = "1234")
    String code
) {
}

