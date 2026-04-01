# Chapitre 09 - Dockerisation

## Objectif

Permettre le demarrage complet de l'application et de ses dependances avec une seule commande.

## Ordre d'implementation

1. creer le `Dockerfile`
2. creer `docker-compose.yml`
3. faire demarrer MySQL, Redis et l'application ensemble

## Fichier 1 - `Dockerfile`

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/cda-java-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Le build se fait en deux temps:
- une image de build pour compiler et produire le jar
- une image d'execution plus legere pour lancer l'application

## Fichier 2 - `docker-compose.yml`

```yaml
services:
  mysql:
    image: mysql:8.0.37
    container_name: cda-mysql
    environment:
      MYSQL_DATABASE: cda_java
      MYSQL_USER: cda
      MYSQL_PASSWORD: cda
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 10

  redis:
    image: redis:7.4-alpine
    container_name: cda-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 10

  app:
    build: .
    container_name: cda-app
    environment:
      DB_URL: jdbc:mysql://mysql:3306/cda_java
      DB_USERNAME: cda
      DB_PASSWORD: cda
      REDIS_HOST: redis
      REDIS_PORT: 6379
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
```

Ce fichier reproduit l'infrastructure minimale du projet final:
- MySQL pour la persistence
- Redis pour le cache
- Spring Boot pour l'application web

## Validation

```bash
docker compose up -d --build
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080
```

## Resultat attendu

- MySQL tourne
- Redis tourne
- l'application Spring Boot tourne
- `http://localhost:8080` retourne `200`
