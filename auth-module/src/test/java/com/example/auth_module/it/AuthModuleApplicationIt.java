package com.example.auth_module.it;

import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.model.UserEntity;
import com.example.auth_module.repository.UserRepository;
import com.example.auth_module.service.AuthService;
import com.example.auth_module.service.EmailSenderIntegrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест регистрации:
 * - поднимаем полный Spring-контекст модуля
 * - Postgres в Testcontainers
 * - миграции Flyway берутся по относительному пути из architect-module (настроено в application-test.properties)
 * - реальный репозиторий/шифрование пароля; почтовый сервис замокан
 */
@SpringBootTest                      // поднимаем весь контекст приложения модуля
@ActiveProfiles("test")              // используем src/test/resources/application-test.properties
@Testcontainers                      // включаем поддержку Testcontainers в JUnit
class AuthServiceRegistrationIt {
    //  БД для тестов — реальный Postgres в контейнере (один на весь класс)
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("rent_apartment_db")
        .withUsername("postgres")
        .withPassword("postgres");
    // Прокидываем параметры подключения контейнера в Spring (без хардкода URL/кредов)
    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }
    // Получаем реальные бины сервиса и репозитория
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;

    // Режем реальную отправку писем — подменяем бин на мок
    @MockBean EmailSenderIntegrationService emailSender;

    @Test
    @DisplayName("registration создает пользователя (GUEST) и пишет код верификации")
    void registration_creates_guest_with_code() {
        // Почта не уезжает наружу — возвращаем фиктивный ответ
        BDDMockito.given(emailSender.sendCodeVerification(BDDMockito.any())).willReturn("OK");

        var req = new UserRequestDto(
            "test@mail",   // loginValue (email)
            "Serega",      // usernameValue
            "123456",      // passwordValue
            null           // code не нужен при регистрации
        );

        var msg = authService.registration(req);
        assertThat(msg).containsIgnoringCase("код отправлен");

        var saved = userRepository.findByLoginCriteria("serega").orElseThrow();
        assertThat(saved.getRoles()).contains(UserEntity.Role.GUEST);
        assertThat(saved.getVerification()).isNotBlank(); // 4-значный код
        assertThat(saved.getPasswordHash()).isNotBlank();
    }
}

