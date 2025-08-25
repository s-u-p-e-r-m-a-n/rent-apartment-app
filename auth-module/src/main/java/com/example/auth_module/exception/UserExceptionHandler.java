package com.example.auth_module.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<String> catchUserException(UserException e) {

        return ResponseEntity.status(e.getErrorCode()).body(e.getMessage());
    }
}
