package com.example.architect_module.service;

import com.example.architect_module.dto.ArchitectMigrationDto;

public interface ArchitectService {

    // создание шаблона для создания файла версии миграции
    // создание шаблона содержимого файла миграции
    public String createNewMigrationFile(ArchitectMigrationDto dto);
}
