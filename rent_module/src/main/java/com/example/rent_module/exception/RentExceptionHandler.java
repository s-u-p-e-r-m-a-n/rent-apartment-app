package com.example.rent_module.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RentExceptionHandler {

    @ExceptionHandler(ApartmentException.class)
    public ResponseEntity<String> catchException(ApartmentException e) {

        return ResponseEntity.status(e.getErrorCode()).body(e.getMessage());
    }
}
