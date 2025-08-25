package com.example.auth_module.dto;

import lombok.Data;

@Data
public class UserRequestDto {

    private String usernameValue;
    private String loginValue;
    private String passwordValue;
    private String code;
}
