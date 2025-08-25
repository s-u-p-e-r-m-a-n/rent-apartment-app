package com.example.architect_module.service.impl;

public class ArchitectStaticExample {

    public static final String NAME_FILE= "C:\\Users\\superman\\IdeaProjects\\rent_apartment_app\\architect_module\\src\\main\\resources\\db\\migration\\V${version}__create_table_${table}.sql";
    public static final String EXAMPLE_CODE_CREATE= """
            create table if not exists ${table}(\
            id bigint primary key,\
            ${code1}
            );\
            
            create sequence ${table}_sequence start 2 increment 1;
            
            insert into ${table}(id,${code2})
            values (${sequensValue})""";
}
