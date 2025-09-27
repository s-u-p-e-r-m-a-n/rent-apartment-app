package com.example.auth_module.it;

import com.example.auth_module.service.EmailSenderIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIT {

    // ---------- Контейнер Postgres: один на весь прогон JVM ----------
    private static final TestPostgresContainer PG = TestPostgresContainer.getInstance();
    static {
        PG.start(); // стартуем ровно один раз и держим порт/БД живыми для всех IT-классов
    }

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", PG::getJdbcUrl);
        r.add("spring.datasource.username", PG::getUsername);
        r.add("spring.datasource.password", PG::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    // ---------- Тестовая инфраструктура ----------
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @MockBean
    protected EmailSenderIntegrationService emailSender;

    @BeforeEach
    void setUpMocks() {
        given(emailSender.sendCodeVerification(any())).willReturn("OK");
    }

    // ---------- Безопасная очистка БД перед каждым тестом ----------
    @BeforeEach
    void cleanupDb() {
        try {
            // health-check — гарантируем, что DataSource/контейнер живы
            jdbcTemplate.execute("SELECT 1");

            // все таблицы схемы public, кроме служебной flyway_schema_history
            var tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables " +
                    "where table_schema = 'public' and table_name <> 'flyway_schema_history'",
                String.class
            );

            if (tables.isEmpty()) return;

            var targets = tables.stream()
                .map(t -> "public.\"" + t + "\"")
                .collect(java.util.stream.Collectors.joining(", "));

            jdbcTemplate.execute("TRUNCATE TABLE " + targets + " RESTART IDENTITY CASCADE");
        } catch (org.springframework.jdbc.CannotGetJdbcConnectionException e) {
            // контейнер/пул уже закрыт — пропускаем очистку, не валим прогон
            System.out.println("DB cleanup skipped: " + e.getMessage());
        } catch (Exception e) {
            // логируем, но не валим весь прогон из-за единичной ошибки очистки
            System.out.println("DB cleanup error: " + e.getMessage());
        }
    }
}
