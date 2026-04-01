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

Ce chapitre pose donc une discipline technique tres importante pour tout le reste du projet.
La base ne sera pas creee "en automatique" par des effets de bord.
Elle sera versionnee, lisible et reproductible, ce qui est beaucoup plus formateur.

### Lecture detaillee de `application.yml`

1. Le noeud `spring.datasource` regroupe la configuration de la base relationnelle.
2. `url` pointe vers MySQL et reste surchargeable par variable d'environnement.
3. `username` et `password` sont egalement parametrables.
4. `driver-class-name` impose explicitement le pilote MySQL.
5. `spring.jpa.hibernate.ddl-auto: validate` demande a Hibernate de verifier, pas de creer.
6. `open-in-view: false` evite de laisser le contexte de persistence ouvert jusqu'a la vue.
7. `hibernate.format_sql: true` rend le SQL plus lisible dans les logs.
8. `spring.flyway.enabled: true` active Flyway au demarrage.
9. `spring.data.redis.host` et `port` declarent le serveur Redis.
10. `spring.cache.type: redis` fait de Redis le backend de cache Spring.
11. `spring.thymeleaf.cache: false` facilite le travail de developpement sur les templates.
12. `server.port` permet de surcharger le port via variable d'environnement.
13. Le niveau de log Spring Security est positionne sur `INFO` pour limiter le bruit tout en gardant de la visibilite.

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

### Lecture detaillee de `application-test.yml`

1. Ce fichier reprend la meme idee que `application.yml`, mais pour le profil `test`.
2. L'URL de datasource pointe vers une base de test theorique `cda_java_test`.
3. Les identifiants `test/test` servent de base de repli.
4. `ddl-auto: validate` est conserve pour garder la meme discipline de schema.
5. Flyway reste actif dans les tests.
6. Redis est aussi declare dans ce profil, car les tests d'integration couvrent egalement le cache.
7. Plus tard, Testcontainers remplacera ces valeurs a l'execution avec de vrais conteneurs isoles.

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

Cette facon de faire apprend aussi une vraie logique de projet.
Avant de coder les entites JPA, on clarifie d'abord le modele de donnees que l'on veut porter en base.

### Lecture detaillee de `V1__create_auth_tables.sql`

1. La table `roles` est creee en premier car elle est referencee ensuite.
2. `id BIGINT PRIMARY KEY AUTO_INCREMENT` definit un identifiant technique simple.
3. `name VARCHAR(50) NOT NULL UNIQUE` impose un nom obligatoire et unique pour chaque role.
4. La table `users` suit ensuite.
5. `username` est unique, ce qui permettra l'authentification par login.
6. `email` est egalement unique.
7. `password` stockera un hash, pas un mot de passe brut.
8. `first_name` et `last_name` sont deja presents car le profil sera complet des le premier parcours.
9. `created_at` garde la date de creation de l'utilisateur.
10. `updated_at` garde la date de derniere modification.
11. La table `user_roles` est la table d'association entre utilisateurs et roles.
12. `PRIMARY KEY (user_id, role_id)` interdit deux fois la meme association.
13. Les contraintes `FOREIGN KEY` garantissent que l'association ne reference que des lignes existantes.

## Fichier 4 - `src/main/resources/db/migration/V2__seed_roles.sql`

```sql
INSERT INTO roles (name) VALUES ('ROLE_USER');
```

Ici, on seed uniquement `ROLE_USER` car c'est le role necessaire au parcours standard d'inscription.

Le fait que `ROLE_ADMIN` n'apparaisse pas encore dans la migration ne bloque pas la suite du cours.
Le code Java connait deja cette valeur, mais l'application finale construite ici repose sur le role standard utilisateur.

### Lecture detaillee de `V2__seed_roles.sql`

1. Cette migration ajoute une premiere donnee metier et pas seulement une structure.
2. `ROLE_USER` est insere des maintenant car le service d'inscription en dependra.
3. Le fait de seed en SQL garantit que le role existe avant toute creation d'utilisateur.
4. Le service metier n'a donc pas a inventer le role: il le recharge depuis la base.

## Validation

Le plus simple est de continuer les chapitres avant de lancer la pile complete.
Mais a ce stade, la configuration centrale et les migrations sont posees.

## Resultat attendu

- la configuration Spring est en place
- le schema est versionne
- la base attend deja les futures entites `User` et `Role`

## Ce Que Ce Chapitre Apporte Au Suivant

Le chapitre suivant peut maintenant creer les entites JPA avec un objectif clair.
Il ne code pas "dans le vide": il s'aligne sur un schema SQL deja pose et deja pense pour l'application finale.
