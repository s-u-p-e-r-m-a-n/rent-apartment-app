package com.example.architect_module.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
public class DumpController {
    // Параметры для контейнера и базы данных
    private static final String DOCKER_CONTAINER_NAME = "postgres-container";
    private static final String DB_USER = "postgres";
    private static final String DB_NAME = "my_database";
    private static final String DUMP_FILE_PATH = "/path/to/dump.sql"; // Укажите путь для дампа

    // Эндпоинт для запуска дампа базы данных
    @GetMapping("/dump")
    public String createDatabaseDump() {
        try {
            // Формируем команду для выполнения дампа
            String command = String.format(
                    "docker exec %s pg_dump -U %s %s > %s",
                    DOCKER_CONTAINER_NAME, DB_USER, DB_NAME, DUMP_FILE_PATH
            );

            // Выполняем команду
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();  // Ждем завершения процесса

            // Чтение вывода процесса (по желанию)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Проверка на ошибки
            if (process.exitValue() != 0) {
                return "Ошибка при создании дампа.";
            }

            return "Дамп базы данных успешно создан!";
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Ошибка при создании дампа: " + e.getMessage();
        }
    }
}
/*
Для того чтобы создать контроллер в Spring Boot, который будет выполнять дамп базы данных PostgreSQL в Docker контейнере, нужно выполнить несколько шагов:

1. **Настроить контейнер Docker с PostgreSQL**.
2. **Написать команду для создания дампа базы данных PostgreSQL**.
3. **Создать Spring Boot приложение с контроллером для вызова этой команды**.

### 1. Подготовка Docker контейнера с PostgreSQL

Если у вас еще нет контейнера с PostgreSQL, то вы можете его создать с помощью следующей команды:

```bash
docker run --name postgres-container -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=root -e POSTGRES_DB=my_database -d postgres:13
```

- Контейнер с PostgreSQL будет создан с именем `postgres-container`.
- База данных `my_database` будет создана автоматически.
- Пользователь базы данных будет `postgres`, а пароль `root`.

Вы можете подключиться к базе данных внутри контейнера с помощью команды:

```bash
docker exec -it postgres-container psql -U postgres -d my_database
```

### 2. Сценарий для дампа базы данных PostgreSQL

Чтобы выполнить дамп базы данных PostgreSQL, можно использовать команду `pg_dump`. В случае с контейнером Docker, команда будет выглядеть так:

```bash
docker exec postgres-container pg_dump -U postgres my_database > dump.sql
```

Этот скрипт выполнит дамп базы данных `my_database` и сохранит его в файл `dump.sql` на хост-системе.

### 3. Реализация контроллера Spring Boot

Теперь давайте реализуем Spring Boot приложение с контроллером, который будет запускать команду для создания дампа базы данных.



- В этом коде используется команда для создания дампа базы данных с помощью `pg_dump` внутри Docker контейнера. Дамп сохраняется в файл, указанный в переменной `DUMP_FILE_PATH`. Убедитесь, что этот путь доступен для записи на хост-машине.
- Вы можете изменить путь к файлу дампа, например, на `/tmp/dump.sql`, чтобы сохранить его в каталоге `/tmp` вашей хост-системы.

#### 3.4. Пример работы

После того как вы запустите приложение, ваш контроллер будет доступен по URL `http://localhost:8080/dump`. При вызове этого эндпоинта будет запускаться процесс создания дампа базы данных и результат возвращаться в ответе.

### 4. Дополнительные улучшения

1. **Параметризация команды**: Если вы хотите, чтобы контроллер поддерживал различные базы данных, контейнеры и другие параметры, можно расширить функциональность через параметры URL или запросы с параметрами (например, `GET /dump?db=my_database`).

2. **Логирование**: Для отладки можно добавить логирование ошибок и выводить результаты выполнения команды.

3. **Безопасность**: В реальных приложениях стоит добавить механизмы авторизации и аутентификации, чтобы только авторизованные пользователи могли вызывать данную операцию.

4. **Обработка ошибок**: Можно улучшить обработку ошибок и возврат более подробной информации о процессе дампа.

### 5. Рекомендуемая структура проекта

Проект может иметь следующую структуру:

```plaintext
src/
 └── main/
     └── java/
         └── com/
             └── example/
                 └── dbdump/
                     ├── DatabaseDumpController.java
                     └── DbdumpApplication.java
 └── resources/
     └── application.properties
```

### Заключение

Теперь у вас есть Spring Boot приложение с REST контроллером, которое выполняет дамп базы данных PostgreSQL, запущенной внутри Docker контейнера. Вы можете вызывать этот процесс через HTTP запросы и получать результат в виде успешного сообщения или ошибки.

 */

