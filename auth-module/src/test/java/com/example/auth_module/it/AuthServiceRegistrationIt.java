package com.example.auth_module.it;

import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.exception.UserException;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

/**
 * Интеграционный тест регистрации:
 * - поднимаем полный Spring-контекст модуля
 * - Postgres в Testcontainers
 * - миграции Flyway берутся по относительному пути из architect-module (настроено в application-test.properties)
 * - реальный репозиторий/шифрование пароля; почтовый сервис замокан
 */

    class AuthServiceRegistrationIt extends BaseIT {

    // Получаем реальные бины сервиса и репозитория
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    private UserRequestDto userRequestDto;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto("Serega",
            "it_user@example.com","123456",null);
            userRepository.deleteAll(); //чистим базу

    }
    @Transactional
    @Test
    @DisplayName("registration создает пользователя (GUEST) и пишет код верификации")
    void registration_creates_guest_with_code() {
        // Почта не уезжает наружу — возвращаем фиктивный ответ

        var msg = authService.registration(userRequestDto);
        assertThat(msg).containsIgnoringCase("код отправлен");

        var saved = userRepository.findByLoginCriteria("it_user@example.com").orElseThrow();
        assertThat(saved.getRoles()).contains(UserEntity.Role.GUEST);
        assertThat(saved.getVerification()).isNotBlank(); // 4-значный код
        assertThat(saved.getPasswordHash()).isNotBlank();
    }

    @Test
    void registration_duplicateEmail_returnsConflict() {
        authService.registration(userRequestDto);

        // второй вызов
        var ex = assertThrows(UserException.class, () -> authService.registration(userRequestDto));
        assertThat(ex.getErrorCode()).isEqualTo(409);
    }

}

