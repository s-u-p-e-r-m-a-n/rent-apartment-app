package com.example.auth_module.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SenderDto {
    private String email;
    private String code;

}
