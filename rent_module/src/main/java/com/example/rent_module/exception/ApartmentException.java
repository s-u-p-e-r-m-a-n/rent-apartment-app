package com.example.rent_module.exception;

import lombok.Data;


@Data
public class ApartmentException extends RuntimeException {
    public static final String APARTMENT_NOT_EXIST = "Аппартаменты не доступны";
    public static final int APARTMENT_NOT_EXIST_CODE = 700;
    private int errorCode;

    public ApartmentException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
