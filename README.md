# Rent Apartment App 🏠

Микросервисная REST-система аренды жилья.  
Функциональность: регистрация и авторизация пользователей (JWT), управление квартирами (CRUD), поиск по параметрам, интеграция с внешним API для геолокации, система уведомлений.

---

## 📂 Модули
- **auth-module** – авторизация и пользователи (JWT, роли)
- **apartment-module** – управление квартирами (CRUD)
- **architect-module** – миграции БД (Flyway)
- **eureka-server** – сервис-реестр (Eureka)
- **api-gateway** – единая точка входа (Spring Cloud Gateway)
- **email-sender** – сервис уведомлений по почте

---

## 🛠️ Стек технологий
- **Backend:** Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA, Spring Cloud Netflix
- **Database:** PostgreSQL, H2 (тесты), Flyway
- **Messaging:** Kafka (event-driven коммуникация)
- **Infra:** Docker Compose, Eureka, API Gateway
- **Testing:** JUnit 5, Mockito
- **CI/CD:** GitHub Actions

---

## 🚀 Запуск локально
1. Поднять инфраструктуру:
   ```bash
   docker-compose up -d
## 🛠️ Прогнать тесты:
./mvnw clean verify
✅ CI

GitHub Actions прогоняет тесты на H2 (memory DB) для проверки миграций и модулей.

Основные миграции разделены:

postgres/ – для продакшн окружения

h2/ – для CI/тестов
