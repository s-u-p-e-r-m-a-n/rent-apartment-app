# Rent Apartment App 🏠

Микросервисная REST-система аренды жилья.  
Функциональность: регистрация и авторизация пользователей (JWT), управление квартирами (CRUD), поиск по параметрам, интеграция с внешним API для определения геолокации.

## 📂 Модули
- **auth-module** – авторизация и пользователи (JWT)
- **apartment-module** – управление квартирами
- **architect-module** – миграции БД (Flyway)
- **eureka-server** – сервис-реестр (Eureka)
- **api-gateway** – единая точка входа
- **email-sender** – сервис уведомлений

## 🛠️ Технологии
Java 17 · Spring Boot 3 · Spring Security (JWT) · REST · PostgreSQL · Flyway · Kafka · Docker Compose · JUnit 5/Mockito · GitHub Actions

## 🚀 Запуск
```bash
docker-compose up -d
mvn clean verify
