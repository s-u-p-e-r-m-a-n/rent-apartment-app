package com.example.architect_module.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FlywaySchema {

    private final JdbcTemplate jdbcTemplate;

    public String getVersion(){

        return jdbcTemplate.queryForObject("select version from flyway_schema_history  order by version desc limit 1", String.class);
    }


}
