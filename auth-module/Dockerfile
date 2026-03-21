# ===== build =====
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml pom.xml
COPY architect-module/pom.xml architect-module/pom.xml
COPY architect-module/src architect-module/src
COPY auth-module/pom.xml auth-module/pom.xml
COPY auth-module/src auth-module/src

# Собираем только auth (миграции скопируются плагином)
RUN mvn -B -f auth-module/pom.xml -DskipTests clean package

# ===== runtime =====
FROM eclipse-temurin:21-jdk
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=builder /build/auth-module/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
