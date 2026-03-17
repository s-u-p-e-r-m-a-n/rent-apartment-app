# 🔐 Auth Module — Spring Boot Authentication Service

> Микросервис аутентификации и авторизации пользователей для проекта **Rent Apartment App**.  
> Готов к использованию как отдельный демонстрационный сервис или часть монорепозитория.

---

## ⚙️ Основные возможности

- Регистрация пользователей с валидацией e-mail.
- Подтверждение регистрации через код (демо — заглушка email-sender).
- Авторизация по логину и паролю.
- Генерация JWT (access) и Refresh токенов.
- Обновление (ротация) токенов.
- Ролевая модель пользователей (`GUEST`, `USER`, `ADMIN`, `SUPER_ADMIN`).
- Админ-эндпоинты для управления ролями.
- Поддержка профилей: `dev`, `test`, `docker`.
- Полная документация через **Swagger UI**.
- Инициализация базы данных с SUPER_ADMIN по умолчанию.

---

## 🧰 Технологии

| Компонент | Статус | Описание                                |
|------------|--------|-----------------------------------------|
| **Java** | ✅ | Версия 17                               |
| **Spring Boot** | ✅ | 3.2.10                                  |
| **Spring Data JPA** | ✅ | Работа с PostgreSQL                     |
| **Spring Security** | ✅ | JWT-аутентификация                      |
| **Spring Validation** | ✅ | Проверка входных DTO                    |
| **Flyway** | ✅ | Миграции базы                           |
| **PostgreSQL** | ✅ | Основная БД                             |
| **JJWT 0.12.6** | ✅ | Работа с JWT                            |
| **Swagger (Springdoc)** | ✅ | API-документация                        |
| **JUnit 5 / Mockito / Testcontainers** | ✅ | Тестирование                            |
| **Docker / Compose** | ✅ | Контейнеризация                         |
| **Lombok / MapStruct** | ✅ | Генерация и маппинг DTO                 |
| **Eureka Client (Netflix)** | ⚙️ | Подключен, но отключен в Docker-профиле |

---

## 🧱 Архитектура проекта (основные файлы и директории)

~~~ text
auth-module/
├── src/main/java/com/example/auth_module/
│   ├── config/           # классы конфигурации
│   ├── controller/       # REST-контроллеры
│   ├── service/          # Сервисы (бизнес-логика)
│   │   ├── impl/         # Реализация сервисов
│   │   └── security/     # JWT, фильтры, конфигурация
│   ├── model/            # JPA-сущности
│   ├── dto/              # DTO (запросы, ответы)
│   ├── repository/       # Репозитории Spring Data
│   └── exception/        # UserException и ApiError
├── src/main/resources/
│   ├── application-docker.properties
│   └── db/migration/postgres/
│       ├── V1__create_table_user_info.sql
│       ├── ...
│       └── V14__create_super_admin.sql
└── Dockerfile
~~~

---

## 🚀 Запуск сервиса (Docker)

### 1. Подготовь `.env`

Создай файл `.env` в корне проекта на основе `.env.example`:
~~~ bash
cp .env.example .env
~~~
Пример .env.example:

 ~~~ env

# База данных
POSTGRES_DB=postgres
POSTGRES_USER=root
POSTGRES_PASSWORD=root

# JWT секрет
JWT_SECRET=ChangeMe_ThisIsA_VeryLongSecretKey_AtLeast32Chars

# Настройки сервиса
AUTH_SERVER_PORT=8081
~~~
### 2. Собери и запусти контейнеры
~~~bash
docker compose up --build -d
~~~
### 3. Проверка

Swagger UI → http://localhost:8081/swagger-ui/index.html

Просмотр логов:
~~~bash
docker compose logs -f auth-module
~~~
🧩 Flyway миграции и SUPER_ADMIN
При запуске в Docker автоматически выполняются все Flyway-скрипты:

auth-module/src/main/resources/db/migration/postgres/

Скрипт V14__create_super_admin.sql создаёт суперпользователя:

~~~ sql

-- Создаём SUPER_ADMIN
INSERT INTO user_info (id, date_registration, login, password_hash, username, verification)
VALUES (
    1,
    now(),
    'superadmin@mail.com',
    '$2a$10$GIG0LCr4wmI9ENvUzbhCkuBzZzug0BK/68M8kiJ1WJZj9Ju5A76Ya', -- хэш пароля:  Admin123!
    'SUPER_ADMIN',
    'verified'
)
ON CONFLICT (id) DO NOTHING;

-- Назначаем роль
INSERT INTO user_roles (user_id, roles)
VALUES (1, 'SUPER_ADMIN')
ON CONFLICT (user_id, roles) DO NOTHING;
~~~
**Данные суперпользователя:**

| Поле | Значение |
|------|-----------|
| username | SUPER_ADMIN |
| login | superadmin@mail.com |
| password | Admin123! |
| roles | SUPER_ADMIN |
| verification | verified |

💡 Этот пользователь создаётся автоматически при первом старте контейнера.
Можно использовать для входа и смены ролей обычных пользователей.

🔑 Основные эндпоинты
Базовый путь: /api/auth

| Метод | URL | Описание |
|--------|-----|----------|
| POST | /registration | Регистрация нового пользователя |
| POST | /authorization | Авторизация (логин + пароль + код) |
| POST | /refresh | Обновление JWT токена |
| PATCH | /admin/{id}/role | Изменение роли пользователя (только ADMIN / SUPER_ADMIN) |

### 📬 Пример регистрации


POST /api/auth/registration
~~~ json
{
  "usernameValue": "Alex",
  "loginValue": "alex@mail.com",
  "passwordValue": "123456",
  "code": ""
}
~~~
Ответ:

~~~ json

"код отправлен"
~~~
В демо-режиме письмо не уходит (mock email sender), код логируется в консоль, где из консоли нужно скопировать
 код при авторизаци и вписать в поле "code".

### 🔐 Авторизация

POST /api/auth/authorization
~~~ json
{
  "usernameValue": "Alex",
  "loginValue": "alex@mail.com",
  "passwordValue": "123456",
  "code": "4321"
}
~~~
Ответ:

~~~ json

{
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "expiryTime": 1730482000
}
~~~
### 🔄 Обновление токена


POST /api/auth/refresh
~~~ json
{
  "refreshToken": "refresh-token"
}
~~~
Ответ:

~~~ json

{
  "accessToken": "new-jwt",
  "refreshToken": "new-refresh"
}
~~~
### ⚙️ Смена роли


PATCH /api/auth/admin/{id}/role
~~~ json
{
  "role": "ADMIN"
}
~~~
### 🧠 Тестирование
Запуск всех тестов:

~~~ bash

cd auth-module
mvn test
~~~
Генерация отчёта покрытия (JaCoCo):

~~~bash

mvn verify
~~~
Результат:
auth-module/target/site/jacoco/index.html

Покрыты:

AuthServiceImpl — регистрация и авторизация;

RefreshTokenService — ротация токенов;

UserAdminServiceImpl — смена ролей;

UserExceptionHandler — MVC-ошибки;

Контроллеры через @WebMvcTest.

### **📘 Swagger UI**
Полная документация API доступна после запуска:

🔗 http://localhost:8081/swagger-ui/index.html

Swagger включает:

Примеры запросов/ответов.

Описания DTO (UserRequestDto, TokenResponseDto, SenderDto).

Глобальные ошибки (ApiError).

Примеры статус-кодов (200, 401, 409, 502).

### 🔗 **Интеграция в монорепозиторий**
Модуль может использоваться как:

самостоятельный Auth-сервис;

часть монорепозитория Rent Apartment App
(в связке с rent_module, email-sender, gateway, eureka-server).
    
При необходимости сервис регистрируется в Eureka и проксируется через API Gateway.

### 🧾 **Обработка ошибок**
Все исключения централизованы через UserExceptionHandler → ApiError.

Пример ответа:

~~~ json

{
  "error": "Invalid or expired refresh token",
  "code": 401,
  "path": "/api/auth/refresh",
  "timestamp": "2025-11-06T14:45:31.123Z"
}
~~~
Типы ошибок:

| Ошибка | Код | Сообщение |
|---------|-----|-----------|
| Повторная регистрация | 409 | "Пользователь с таким логином уже существует" |
| Некорректный e-mail | 400 | "Invalid email format" |
| Неверный пароль | 401 | "Unauthorized" |
| Просроченный токен | 401 | "Invalid or expired refresh token" |
| Ошибка интеграции | 502 | "Error send message" |

---
## 👨‍💻 Разработчик
Sergey A. — ** Java Backend Developer **
- GitHub: [SergeyJavaDev](https://github.com/s-u-p-e-r-m-a-n/rent-apartment-app.git)
- Email: [sergey.javadev@mail.ru](mailto:sergey.javadev@mail.ru)

---
## 📄 Лицензия
Проект распространяется под MIT License.
Разрешается использовать код для pet- и учебных целей.
---
## 💬 Примечание
Auth Module — полностью автономный сервис авторизации.
При интеграции с другими модулями монорепозитория (rent_module, email-sender, gateway, eureka-server)
он будет регистрироваться в Eureka и взаимодействовать с остальными микросервисами через API Gateway.

В текущем виде модуль оптимизирован для демонстрации на GitHub и фриланс-портфолио.
