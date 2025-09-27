package com.example.auth_module.it;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgresContainer extends PostgreSQLContainer<TestPostgresContainer> {
    private static final String IMAGE = "postgres:16-alpine";
    private static TestPostgresContainer INSTANCE;

    private TestPostgresContainer() {
        super(IMAGE);
        withDatabaseName("rent_apartment_db");
        withUsername("postgres");
        withPassword("postgres");
        withReuse(true); // ускоряет локально, порт не «скачет»
    }

    public static synchronized TestPostgresContainer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestPostgresContainer();
        }
        return INSTANCE;
    }
}
