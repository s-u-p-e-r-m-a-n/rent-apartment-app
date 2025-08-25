package com.example.architect_module;


import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class FlywayMigrationsStandaloneTest {

    @Test
    void migrateAll() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:rentapp;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");

        Flyway flyway = Flyway.configure()
            .dataSource("jdbc:h2:mem:rentapp;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1", "sa", "")
            .locations("classpath:db/migration/h2")   // <-- тут важное изменение
            .cleanDisabled(false)
            .load();

        flyway.clean();   // база in-memory, вычищаем перед миграцией на всякий случай
        flyway.migrate(); // прогоняем все миграции
    }
}
