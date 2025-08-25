# Базовый образ с Java 17
FROM eclipse-temurin:17-jdk-alpine

# Создаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем JAR-файл приложения в контейнер
# ЗАМЕНИТЕ имя-файла.jar на реальное имя вашего JAR
COPY target/auth-module-*.jar app.jar

# Открываем порт, который использует приложение
EXPOSE 3700

# Команда запуска приложения при старте контейнера
ENTRYPOINT ["java", "-jar", "app.jar"]
#FROM eclipse-temurin:17-jdk as builder
#WORKDIR /app
#COPY . .
#RUN ./mvnw clean package -DskipTests
#
#FROM eclipse-temurin:17-jre
#WORKDIR /app
#COPY --from=builder /app/target/*.jar app.jar
#EXPOSE 3700
#ENTRYPOINT ["java", "-jar", "app.jar"]