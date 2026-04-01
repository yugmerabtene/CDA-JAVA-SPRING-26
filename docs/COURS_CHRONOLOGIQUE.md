# Parcours de cours chronologique (brique par brique)

Ce document sert de fil conducteur pedagogique pour construire l'application dans un ordre logique.
L'idee est de ne jamais sauter d'etape: chaque chapitre livre une brique, un test, et un resultat verifiable.
Pour l'explication pedagogique riche, les schemas ASCII et les exercices corriges, suivre `docs/SYLLABUS.md` puis `docs/chapitres/README.md`.

## Regle de progression

- Une etape = un objectif clair + des fichiers + un test + un resultat attendu.
- On ne passe pas au chapitre suivant si le test du chapitre courant n'est pas vert.
- Docker et tests sont integres au parcours, pas traites en fin de projet.

## Chapitre 00 - Demarrage projet

Objectif:
- Avoir un squelette compilable et testable.

A produire:
- `pom.xml`
- `src/main/java/com/cda/cdajava/CdaJavaApplication.java`
- `src/test/java/com/cda/cdajava/CdaJavaApplicationTests.java`

Validation:
- build test vert.

Commande:
```bash
docker run --rm -v "/home/yug/Documents/cda-java":/workspace -v /var/run/docker.sock:/var/run/docker.sock -w /workspace maven:3.9.9-eclipse-temurin-17 mvn test
```

Resultat attendu:
- le projet compile, au moins un test passe.

## Chapitre 01 - SQL + migrations

Objectif:
- Mettre en place MySQL et la migration schema.

A produire:
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/V1__create_auth_tables.sql`
- `src/main/resources/db/migration/V2__seed_roles.sql`

Validation:
- Flyway applique les scripts sans erreur.

Resultat attendu:
- tables `users`, `roles`, `user_roles` existantes.

## Chapitre 02 - Modele metier + enum roles

Objectif:
- Poser les entites et fiabiliser les roles via enum.

A produire:
- `src/main/java/com/cda/cdajava/model/User.java`
- `src/main/java/com/cda/cdajava/model/Role.java`
- `src/main/java/com/cda/cdajava/model/RoleName.java`

Validation:
- mapping JPA valide.

Resultat attendu:
- `Role.name` est un enum persiste en texte (`ROLE_USER`, `ROLE_ADMIN`).

## Chapitre 03 - Repository + DAO

Objectif:
- Separer acces donnees standard et acces metier.

A produire:
- `src/main/java/com/cda/cdajava/repository/UserRepository.java`
- `src/main/java/com/cda/cdajava/repository/RoleRepository.java`
- `src/main/java/com/cda/cdajava/dao/UserDao.java`
- `src/main/java/com/cda/cdajava/dao/RoleDao.java`
- `src/main/java/com/cda/cdajava/dao/impl/UserDaoImpl.java`
- `src/main/java/com/cda/cdajava/dao/impl/RoleDaoImpl.java`

Validation:
- recherches utilisateur/role fonctionnelles.

## Chapitre 04 - DTO + mapper + service inscription

Objectif:
- Isoler les contrats d'entree/sortie et coder la logique metier d'inscription.

A produire:
- `src/main/java/com/cda/cdajava/dto/RegisterRequestDto.java`
- `src/main/java/com/cda/cdajava/mapper/UserMapper.java`
- `src/main/java/com/cda/cdajava/service/AuthService.java`
- `src/main/java/com/cda/cdajava/service/impl/AuthServiceImpl.java`

Validation:
- hash mot de passe + affectation du role par defaut.

Test associe:
- `src/test/java/com/cda/cdajava/service/AuthServiceImplTest.java`

## Chapitre 05 - MVC frontend (Bootstrap)

Objectif:
- Exposer un parcours utilisateur complet: accueil, inscription, login, profil.

A produire:
- `src/main/java/com/cda/cdajava/controller/HomeController.java`
- `src/main/java/com/cda/cdajava/controller/AuthController.java`
- `src/main/java/com/cda/cdajava/controller/ProfileController.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/auth/register.html`
- `src/main/resources/templates/auth/login.html`
- `src/main/resources/templates/profile/profile.html`

Validation:
- formulaires affiches, routes atteignables.

Test associe:
- `src/test/java/com/cda/cdajava/controller/AuthControllerWebMvcTest.java`

## Chapitre 06 - Securite Spring

Objectif:
- Proteger les routes privees et connecter l'authentification a la base.

A produire:
- `src/main/java/com/cda/cdajava/config/SecurityConfig.java`
- `src/main/java/com/cda/cdajava/security/CustomUserDetailsService.java`

Validation:
- `/profile` inaccessible sans login, login fonctionnel avec utilisateur cree.

## Chapitre 07 - Redis cache

Objectif:
- Mettre en cache la lecture profil, invalider a la mise a jour.

A produire:
- `src/main/java/com/cda/cdajava/config/CacheConfig.java`
- `src/main/java/com/cda/cdajava/service/UserService.java`
- `src/main/java/com/cda/cdajava/service/impl/UserServiceImpl.java`

Validation:
- hit cache apres lecture, eviction apres update.

Test associe:
- `src/test/java/com/cda/cdajava/integration/RedisCacheIT.java`

## Chapitre 08 - Docker + integration complete

Objectif:
- Executer l'application complete avec dependances infra.

A produire:
- `Dockerfile`
- `docker-compose.yml`

Validation:
- stack app/mysql/redis demarre correctement.

Commande:
```bash
docker compose up -d --build
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080
```

Resultat attendu:
- code HTTP `200`.

## Chapitre 09 - Integration tests end-to-end

Objectif:
- Verifier les flux reels avec Testcontainers.

A produire:
- `src/test/java/com/cda/cdajava/integration/AbstractContainerIT.java`
- `src/test/java/com/cda/cdajava/integration/AuthFlowIT.java`

Validation:
- tests integration verts sur MySQL + Redis reels (conteneurs).

## Point de controle final

Checklist:
- architecture multicouche respectee
- roles geres par enum (`RoleName`)
- inscription/connexion/profil fonctionnels
- cache Redis actif
- docker compose operationnel
- tests unitaires + integration verts
