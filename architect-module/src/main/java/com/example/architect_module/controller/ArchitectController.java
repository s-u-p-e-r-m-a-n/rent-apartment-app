package com.example.architect_module.controller;

import com.example.architect_module.dto.ArchitectMigrationDto;
import com.example.architect_module.service.ArchitectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArchitectController {

    private final ArchitectService architectService;

    @Autowired
    ArchitectController(ArchitectService architectService) {
        this.architectService = architectService;
    }


    @PostMapping("/architect/create")
    public String createArchitect(@RequestBody ArchitectMigrationDto architectMigrationDto) {

        return architectService.createNewMigrationFile(architectMigrationDto);
    }

}
