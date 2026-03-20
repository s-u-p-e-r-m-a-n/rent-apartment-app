
# 🏠 Rent Apartment App — Java Spring Boot microservices

[![Release](https://img.shields.io/github/v/release/s-u-p-e-r-m-a-n/rent-apartment-app?include_prereleases)](../../releases)

[![CI](https://github.com/s-u-p-e-r-m-a-n/rent-apartment-app/actions/workflows/ci.yml/badge.svg)](https://github.com/s-u-p-e-r-m-a-n/rent-apartment-app/actions/workflows/ci.yml)




Монорепозиторий backend-проекта сервиса аренды жилья.  
Архитектура построена вокруг отдельных модулей: `auth-module`, `rent_module`, `email-sender`, `api-gateway`, `eureka-server` и вспомогательной инфраструктуры.

На текущем этапе `auth-module` является наиболее оформленным showcase-ready модулем, а остальные части проекта находятся в активной разработке как часть общей платформы.

---

## 🧩 Модули

### 🔐 `auth-module` — сервис аутентификации и авторизации

Отдельный микросервис авторизации пользователей:

- регистрация с валидацией e-mail;
- подтверждение регистрации через код (демо: mock-логика вместо реального email-sender);
- логин по логину и паролю;
- JWT (access + refresh), ротация токенов;
- ролевая модель (`GUEST`, `USER`, `ADMIN`, `SUPER_ADMIN`);
- админ-эндпоинты для управления ролями;
- профили `dev`, `test`, `docker`;
- Flyway-миграции, автоинициализация SUPER_ADMIN;
- Swagger UI;
- unit + integration тесты, отчёт покрытия (JaCoCo);
- Dockerfile, запуск через `docker compose`;
- готов к использованию как отдельный сервис или часть микросервисной схемы.

📄 Подробности — в [`auth-module/README.md`](auth-module/README.md).

---

### 🏡 `rent_module` — апартаменты, фото, рейтинги, интеграции

Работающий микросервис бизнес-логики сервиса аренды:

- CRUD для апартаментов;
- привязка апартаментов к пользователям;
- загрузка и хранение фотографий;
- комментарии и рейтинги пользователей;
- интеграция с геолокацией (Яндекс API через `RestTemplate`);
- сбор статистики и перерасчёт рейтингов (AOP + scheduler);
- собственная модель пользователей внутри модуля (для локальной работы и разработки).

⚙️ Текущий статус:

- модуль запускается из IntelliJ IDEA с профилем `dev` и подключённой БД;
- бизнес-логика и интеграции работают;
- тестовое покрытие и Swagger-документация ещё не доведены до уровня `auth-module`
  (развитие ведётся по мере свободного времени).

---

### 🧱 `architect-module` — миграции БД

- централизованные Flyway-миграции для основных таблиц;
- используется при сборке и инициализации схемы БД;
- помогает поддерживать единый контракт данных между сервисами.

---

### ✉️ `email-sender` — сервис отправки писем

Отдельный микросервис для работы с e-mail:

- на текущем этапе реализована базовая логика (отправка/имитация отправки писем);
- используется в связке с авторизацией (подтверждение регистрации, коды);
- может запускаться отдельно из IDE;
- в планах — расширение функционала и вынесение всей e-mail-логики из `auth-module`.

---

### 🌐 `gateway` — API Gateway

Микросервис для маршрутизации запросов:

- принимает внешние запросы и проксирует их к внутренним сервисам (`auth-module`, `rent_module` и др.);
- интегрируется с Eureka для поиска живых инстансов;
- может запускаться отдельно для демонстрации схемы “клиент → gateway → микросервисы”;
- конфигурация будет постепенно расширяться по мере развития проекта.

---

### 📡 `eureka-server` — сервис-дискавери

Сервис регистрации и обнаружения микросервисов:

- регистрирует `auth-module`, `rent_module`, `email-sender`, `gateway` и другие сервисы;
- позволяет строить микросервисную схему без жёстко прошитых адресов;
- уже может быть запущен из IDE как отдельный сервис;
- используется как основа для демонстрации микросервисной архитектуры.

---

## 🧰 Технологии и где используются

- **Java 17** — во всех модулях.  
- **Spring Boot 3.x** — основа для `auth-module`, `rent_module`, `email-sender`, `gateway`, `eureka-server`.  
- **Spring Security + JWT (JJWT 0.12.6)** — `auth-module` (аутентификация, авторизация, фильтры).  
- **Spring Data JPA / Hibernate** — `auth-module`, `rent_module`, `architect-module` (работа с PostgreSQL).  
- **PostgreSQL** — основная БД для авторизации и доменной логики аренды.  
- **Flyway** — миграции схемы БД (`architect-module`, `auth-module`).  
- **Swagger (springdoc-openapi)** — документация API для `auth-module`, планируется для `rent_module`.  
- **JUnit 5, Mockito, Testcontainers, JaCoCo** — тесты и покрытие в `auth-module`, постепенно добавляются в `rent_module`.  
- **Lombok** — модели, DTO и сервисы во всех модулях (удаление бойлерплейта).  
- **MapStruct** — маппинг Entity ↔ DTO в `auth-module` и `rent_module`.  
- **RestTemplate** — интеграция `rent_module` с внешним API (Яндекс геолокация).  
- **AOP + @Scheduled (Spring)** — сбор статистики и пересчёт рейтингов в `rent_module`.  
- **Docker / Docker Compose** — контейнеризация, запуск `auth-module` и PostgreSQL, расширяемо для других сервисов.  
- **Eureka** — сервис-дискавери для микросервисов (`auth-module`, `rent_module`, `email-sender`, `gateway`).

---

## 🚀 Быстрый старт (демо Auth + БД)

На текущем этапе демо-запуск ориентирован на показ работы `auth-module`.  
`rent_module` и остальные сервисы поднимаются из IntelliJ IDEA.


### 1. Требования

- JDK 17  
- Maven 3.9+ (или wrapper)  
- Docker + Docker Compose

### 2. Клонирование репозитория

~~~bash
git clone https://github.com/s-u-p-e-r-m-a-n/rent-apartment-app.git cd rent_apartment_app
~~~
### 3. Подготовка .env
Создай файл .env в корне по аналогии с .env.example (используется для auth-module):

~~~bash

cp .env.example .env
~~~
Пример содержимого — см. в auth-module/README.md.

### 4. Поднять базу и auth-module через Docker
~~~bash

docker compose up --build -d
~~~

---
## Запуск из публичного образа (GHCR)
### Через docker compose (рекомендуется)
Из корня репозитория, где лежат `docker-compose.yml` и `docker-compose.image.yml`:
~~~bash
docker compose -f docker-compose.yml -f docker-compose.image.yml up -d --no-build
docker compose ps
~~~


Swagger UI для Auth:
👉 http://localhost:8081/swagger-ui/index.html

### 5. Запуск остальных сервисов из IDE
Пока без общего docker-compose, через IntelliJ IDEA:

Открыть проект в IntelliJ IDEA.

Настроить profile (например, dev) с подключением к той же PostgreSQL.

Запускать по необходимости:

- rent_module — основной класс приложения, профиль dev;

- email-sender;

- gateway;

- eureka-server.

## 📌 Статус проекта
✅ `auth-module` — showcase-ready модуль  
Код, тесты, Swagger, Docker, Flyway, профили запуска.

⚙️ rent_module — основной бизнес-модуль платформы в активной доработке  
CRUD, фото, комментарии, рейтинги, интеграции и доменная логика уже реализованы; сейчас модуль проходит архитектурное усиление: переработку сущностей и связей, уточнение бизнес-логики и дальнейшее развитие документации и тестов.

⚙️ `email-sender`, `gateway`, `eureka-server` — инфраструктурные сервисы платформы  
Поддерживаются в составе монорепозитория и постепенно дорабатываются вместе с общей архитектурой проекта.

Проект развивается как основной backend-monorepo платформы аренды жилья, где `auth-module` уже оформлен как наиболее зрелый showcase, а остальные модули продолжают развиваться как часть общей системы.

---
## 👨‍💻 Разработчик
Sergey A. — **Java Backend Developer**

- GitHub: [SergeyJavaDev](https://github.com/s-u-p-e-r-m-a-n)
- Email: [sergey.javadev@mail.ru](mailto:sergey.javadev@mail.ru)


📄 Лицензия
Проект распространяется под MIT License и может свободно использоваться, изменяться и адаптироваться в соответствии с условиями лицензии.

