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

Ce fichier est important parce qu'il traduit le projet Java en unite deployable.
Jusqu'ici, on avait surtout pense en termes de code source et d'execution locale.
Avec le `Dockerfile`, on commence a penser en termes d'artefact livrable.

### Lecture detaillee de `Dockerfile`

1. `FROM maven:3.9.9-eclipse-temurin-17 AS build` ouvre une premiere etape de build.
2. `WORKDIR /workspace` fixe le dossier de travail dans l'image.
3. `COPY pom.xml .` copie le descripteur Maven.
4. `COPY src ./src` copie tout le code source.
5. `RUN mvn -q -DskipTests clean package` compile et package le projet en jar.
6. `FROM eclipse-temurin:17-jre` ouvre ensuite une image d'execution plus legere.
7. `WORKDIR /app` fixe le dossier de lancement.
8. `COPY --from=build ... app.jar` recupere le jar produit lors de la premiere etape.
9. `EXPOSE 8080` documente le port de l'application.
10. `ENTRYPOINT [...]` lance le jar Spring Boot au demarrage du conteneur.

Le choix d'un build multi-stage est tres formateur.
Il montre qu'on peut separer:
- l'environnement de build, plus lourd
- l'environnement d'execution, plus simple et plus leger

Cela produit generalement une image plus propre pour le runtime.

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

Il faut bien comprendre ce que `docker-compose.yml` apporte au projet.
Il ne remplace pas la configuration Spring.
Il orchestre les services qui permettront a cette configuration Spring de fonctionner dans un environnement complet.

### Lecture detaillee de `docker-compose.yml`

1. Le service `mysql` utilise l'image `mysql:8.0.37`.
2. `MYSQL_DATABASE`, `MYSQL_USER` et `MYSQL_PASSWORD` creent l'environnement de base du projet.
3. `ports: "3306:3306"` expose MySQL en local.
4. Le `healthcheck` attend que MySQL reponde correctement.
5. Le service `redis` utilise `redis:7.4-alpine`.
6. `ports: "6379:6379"` expose Redis en local.
7. Son `healthcheck` utilise `redis-cli ping`.
8. Le service `app` est construit depuis le `Dockerfile` du projet.
9. Les variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST` et `REDIS_PORT` surchargent `application.yml`.
10. `DB_URL` pointe vers `mysql` et non vers `localhost`, car les conteneurs communiquent par nom de service.
11. `depends_on` avec `condition: service_healthy` retarde le demarrage de l'application tant que MySQL et Redis ne sont pas prets.
12. `ports: "8080:8080"` expose l'application web sur la machine locale.

Ce chapitre est souvent un moment charniere dans un cours.
On passe d'une application qui fonctionne sur une machine de dev a une application qui sait aussi s'executer dans une stack reproductible.

Cette reproductibilite est tres importante pour:
- les demonstrations
- la correction des exercices
- les tests sur d'autres machines
- la livraison future du projet

## Validation

```bash
docker compose up -d --build
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080
```

La premiere commande construit l'image puis demarre MySQL, Redis et l'application.
La seconde verifie rapidement que le serveur web repond bien sur le port 8080.

Le fait d'avoir une validation courte et concrete est utile dans un cours.
On peut tres vite verifier si la stack complete est operationnelle sans devoir tout tester manuellement a ce stade.

## Resultat attendu

- MySQL tourne
- Redis tourne
- l'application Spring Boot tourne
- `http://localhost:8080` retourne `200`

Quand ce chapitre est termine, le projet n'est plus seulement un ensemble de fichiers sources.
Il devient une application complete, demarrable de facon reproductible avec son infrastructure minimale.
