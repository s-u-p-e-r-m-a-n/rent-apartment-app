package com.example.emailsender.dto;

import lombok.Data;


public class SenderDto {

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    private String email;
    private String code;

}
