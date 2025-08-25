package com.example.architect_module.service.impl;

import com.example.architect_module.dto.ArchitectMigrationDto;
import com.example.architect_module.repository.FlywaySchema;
import com.example.architect_module.service.ArchitectService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.example.architect_module.service.impl.ArchitectStaticExample.EXAMPLE_CODE_CREATE;
import static com.example.architect_module.service.impl.ArchitectStaticExample.NAME_FILE;

@RequiredArgsConstructor
@Service
public class ArchitectServiceImpl implements ArchitectService {

    private final FlywaySchema flywaySchema;

    @Override
    public String createNewMigrationFile(ArchitectMigrationDto dto) {
        int oldVersion= Integer.valueOf(flywaySchema.getVersion());
        int newVersion = oldVersion+1;
        Map<String,String> nameFile=new HashMap<>();
        nameFile.put("version",String.valueOf(newVersion));
        nameFile.put("table",dto.getNameTable());
        nameFile.put("code1", dto.getCreateTableCode());
        nameFile.put("code2", dto.getCreateSequensCode());
        nameFile.put("sequensValue", dto.getCreateSequensValue());

        var sub = new StringSubstitutor(nameFile);
        String newPatch = sub.replace(NAME_FILE);
        System.out.println(newPatch);
        var sub1 = new StringSubstitutor(nameFile);
        String newCode = sub1.replace(EXAMPLE_CODE_CREATE);
        System.out.println(newCode);

        Path path = Paths.get(newPatch);
        try {

            byte[] bs = newCode.getBytes();
            Path writtenFilePath = Files.write(path, bs);
            System.out.println("Written content in file:\n"+ new String(Files.readAllBytes(writtenFilePath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return " файл миграции успешно создан ";
    }

}
