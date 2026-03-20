# ===== build =====
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY auth-module/pom.xml pom.xml
COPY auth-module/src src

RUN mvn -B -DskipTests clean package

# ===== runtime =====
FROM eclipse-temurin:21-jdk
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
