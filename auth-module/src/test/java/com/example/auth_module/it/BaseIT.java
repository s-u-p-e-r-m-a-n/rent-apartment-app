package com.example.auth_module.it;

import com.example.auth_module.dto.UserRequestDto;
import com.example.auth_module.service.EmailSenderIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.BDDMockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

// поднимаем весь контекст приложения модуля
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")              // используем src/test/resources/application-test.properties
@Testcontainers                      // включаем поддержку Testcontainers в JUnit
public abstract class BaseIT {

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

    @MockBean
    protected EmailSenderIntegrationService emailSender;
    private UserRequestDto userRequestDto;
    @BeforeEach
    void setUp() {
        given(emailSender.sendCodeVerification(any())).willReturn("OK");
    }

}
