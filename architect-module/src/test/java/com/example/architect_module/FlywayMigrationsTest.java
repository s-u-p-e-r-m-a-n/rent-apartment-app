package com.example.architect_module;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
@RequiredArgsConstructor
@SpringBootTest
@Disabled("Проверка миграций делается в FlywayMigrationsStandaloneTest на H2")
@ActiveProfiles("test")
class FlywayMigrationsTest {

    private Flyway flyway;

    @Test
    void migrateAll() {
        // H2 в памяти каждый раз создаётся заново,
        // поэтому clean() не нужен — только migrate()
        flyway.migrate();
    }
}
