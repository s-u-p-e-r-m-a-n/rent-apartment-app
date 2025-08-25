package com.example.auth_module.exception;

import lombok.Data;

@Data
public class UserException extends RuntimeException{




    public static final  String REGISTRATION_FAILED = "Такой пользователь уже существует";
    public static final  String AUTHORIZATION_TOKEN_FAILED = "Авторизируйтесь";
    public static final String ACCESS_DENIED = "Доступ запрещен";
    public static final  String VALIDATION_FAILED = "Проверьте правильность написания e-mail";
    public static final int VALIDATION_FAILED_CODE = 701;
    public static final  int AUTHORIZATION_TOKEN_FAILED_CODE =704;
    public static final int REGISTRATION_FAILED_CODE = 700;
    public static final String REGISTRATION_WITH_LOGIN_FAILED = "Пользователь с таким  логином существует";
    public static final int REGISTRATION_WITH_LOGIN_FAILED_CODE = 705;
    public static final String USER_DOES_NOT_EXIST = "Пользователь не существует";
    public static final int USER_DOES_NOT_EXIST_CODE = 706;
    public static final String UNCONFIRMED_EMAIL = "Не подтвержденный email или неправильный код";
    public static final int UNCONFIRMED_EMAIL_CODE =707;
    public static final String WRONG_PASSWORD = "Неверный пароль";
    private int errorCode;


    public UserException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;

    }

}
