# Chapitre 01 - Configuration et Flyway

## Objectif

Ajouter la configuration applicative, declarer MySQL et Redis, puis versionner le schema avec Flyway.

## Ordre d'implementation

1. creer `application.yml`
2. creer `application-test.yml`
3. creer la migration du schema
4. creer la migration de seed des roles
5. verifier que la structure SQL est compatible avec le modele vise

## Fichier 1 - `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/cda_java}
    username: ${DB_USERNAME:cda}
    password: ${DB_PASSWORD:cda}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  cache:
    type: redis
  thymeleaf:
    cache: false

server:
  port: ${SERVER_PORT:8080}

logging:
  level:
    org.springframework.security: INFO
```

Points importants poses ici:
- `ddl-auto: validate` force Hibernate a verifier le schema sans le creer
- `flyway.enabled: true` fait des migrations SQL la source de verite
- `cache.type: redis` prepare deja l'etape cache du projet

## Fichier 2 - `src/main/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cda_java_test
    username: test
    password: test
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis
```

Ce fichier prepare le terrain pour les tests. Plus tard, Testcontainers remplacera dynamiquement ces valeurs.

Autrement dit, on pose ici une configuration de reference, mais on ne fige pas encore l'infrastructure de test.

## Fichier 3 - `src/main/resources/db/migration/V1__create_auth_tables.sql`

```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);
```

Le schema est deja pense pour l'application finale:
- une table `users`
- une table `roles`
- une table d'association `user_roles`

L'ordre est important.
On cree d'abord le schema, puis seulement apres on creera les classes Java qui devront lui correspondre exactement.

## Fichier 4 - `src/main/resources/db/migration/V2__seed_roles.sql`

```sql
INSERT INTO roles (name) VALUES ('ROLE_USER');
```

Ici, on seed uniquement `ROLE_USER` car c'est le role necessaire au parcours standard d'inscription.

Le fait que `ROLE_ADMIN` n'apparaisse pas encore dans la migration ne bloque pas la suite du cours.
Le code Java connait deja cette valeur, mais l'application finale construite ici repose sur le role standard utilisateur.

## Validation

Le plus simple est de continuer les chapitres avant de lancer la pile complete.
Mais a ce stade, la configuration centrale et les migrations sont posees.

## Resultat attendu

- la configuration Spring est en place
- le schema est versionne
- la base attend deja les futures entites `User` et `Role`
