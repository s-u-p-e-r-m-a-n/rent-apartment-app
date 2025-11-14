package com.example.auth_module.unit.service;

import com.example.auth_module.service.impl.UserValidationImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserValidationServiceTest {

    @InjectMocks
    UserValidationImpl userValidation;

    @Test
    @DisplayName("валидный e-mail->ок")
    void validService_success() {
        String validMail = "user@mail.ru";
        assertEquals(validMail, userValidation.validation(validMail));
    }

    @Test
    @DisplayName("не валидный e-mail->IllegalArgumentException(Invalid email format)")
    void validService_exception() {
        String badMail = "user@mail";
        var ex = assertThrows(IllegalArgumentException.class, () -> userValidation.validation(badMail));
        assertEquals("Invalid email format", ex.getMessage());
    }
    @Test
    @DisplayName("проверка поведения при значении null->NullPointerException")
    void validService_null_exception() {

        assertThrows(NullPointerException.class, () -> userValidation.validation(null));

    }

}
