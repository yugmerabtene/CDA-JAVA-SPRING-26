# Syllabus projet pas a pas

Ce document est le point d'entree du cours.
Le principe est simple:
- on comprend la brique a construire
- on lit le chapitre associe
- on code ou on relit les fichiers concernes
- on valide la brique avant de passer a la suivante

Le projet final couvre:
- Spring Boot MVC
- Thymeleaf + Bootstrap
- MySQL + Flyway
- Redis cache
- Spring Security
- architecture MVC + Service + Repository + DAO + DTO
- Docker Compose
- tests unitaires, MVC et integration

Ordre de lecture recommande:

1. ce `SYLLABUS.md`
2. `chapitres/README.md`
3. puis les chapitres dans l'ordre numerique

Structure pedagogique de chaque chapitre:
- objectif
- ordre chronologique d'implementation
- fichiers complets a creer ou mettre a jour
- commande de validation
- resultat attendu avant de passer a la suite

## Vue globale du parcours

```text
00. Prerequis et socle
01. Configuration et Flyway
02. Modele JPA et enum roles
03. Repository et DAO
04. DTO, mapper et services
05. MVC, Thymeleaf et Bootstrap
06. Spring Security
07. Cache Redis
08. Erreurs metier
09. Dockerisation
10. Tests d'integration
```

## Logique d'ensemble du projet

```text
Navigateur
   |
   v
Controller MVC
   |
   v
Service metier
   |
   +--> DAO --> Repository --> MySQL
   |
   +--> Cache Spring --> Redis
   |
   +--> Mapper --> DTO
```

## Regles du parcours

- ne pas sauter de chapitre
- ne pas modifier l'architecture pour aller plus vite
- verifier chaque commande avant de continuer
- garder le code et le cours strictement alignes

## Chapitre 0 - Initialisation du socle

Contexte:
- Poser un projet propre, compilable et testable des le debut.

Objectifs:
- Creer la base Maven + Spring Boot.
- Configurer profils (`default`, `test`).

Code a produire:
- `pom.xml`
- `src/main/java/com/cda/cdajava/CdaJavaApplication.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-test.yml`

Tests:
- test de contexte qui demarre.

Resultat attendu:
- `mvn clean test` vert.

## Chapitre 1 - Couche modele + SQL

Contexte:
- Le coeur metier est centre sur un utilisateur avec roles.

Objectifs:
- Modeliser `User` et `Role`.
- Persister avec MySQL via JPA.
- Versionner schema avec Flyway.

Code a produire:
- `model/User.java`, `model/Role.java`
- `repository/UserRepository.java`, `repository/RoleRepository.java`
- `db/migration/V1__create_auth_tables.sql`
- `db/migration/V2__seed_roles.sql`

Tests:
- test d'integration de persistance utilisateur.

Resultat attendu:
- tables creees automatiquement au demarrage.

## Chapitre 2 - DAO + DTO + Mapper + Services

Contexte:
- Eviter d'exposer les entites partout et conserver une logique metier centralisee.

Objectifs:
- Introduire DTO pour entrees/sorties.
- Ajouter couche DAO pour acces donnees metier.
- Ajouter couche service transactionnelle.

Code a produire:
- `dto/RegisterRequestDto.java`, `dto/ProfileDto.java`, `dto/UpdateProfileDto.java`
- `dao/*.java`, `dao/impl/*.java`
- `mapper/UserMapper.java`
- `service/AuthService.java`, `service/UserService.java`, `service/impl/*.java`

Tests:
- unitaires service registration.

Resultat attendu:
- creation utilisateur avec hash mot de passe et role par defaut.

## Chapitre 3 - MVC + frontend Bootstrap

Contexte:
- Besoin d'une interface serveur-side simple, responsive, rapide.

Objectifs:
- Pages `index`, `register`, `login`, `profile`.
- Formulaires valides et messages d'erreur.

Code a produire:
- `controller/HomeController.java`
- `controller/AuthController.java`
- `controller/ProfileController.java`
- `templates/index.html`
- `templates/auth/*.html`
- `templates/profile/profile.html`
- `static/css/app.css`

Tests:
- `AuthControllerWebMvcTest`.

Resultat attendu:
- navigation complete inscription -> connexion -> profil.

## Chapitre 4 - Securite Spring

Contexte:
- Les endpoints profil doivent etre proteges.

Objectifs:
- Configurer Spring Security form-login.
- Integrer `CustomUserDetailsService`.
- Encoder les mots de passe en `BCrypt`.

Code a produire:
- `config/SecurityConfig.java`
- `security/CustomUserDetailsService.java`

Tests:
- tests login et protection des routes via MockMvc.

Resultat attendu:
- `/profile` accessible uniquement apres authentification.

## Chapitre 5 - Cache Redis NoSQL

Contexte:
- Les lectures de profil sont frequentes et candidates au cache.

Objectifs:
- Activer cache Spring.
- Mettre en cache lecture profil.
- Invalider le cache sur mise a jour.

Code a produire:
- `config/CacheConfig.java`
- annotations cache dans `service/impl/UserServiceImpl.java`

Tests:
- integration Redis avec Testcontainers.

Resultat attendu:
- lecture profile cachee, eviction apres update.

## Chapitre 6 - Gestion erreurs

Contexte:
- Centraliser les erreurs metier pour une UX coherent.

Objectifs:
- Definir exception metier.
- Ajouter handler global.

Code a produire:
- `exception/BusinessException.java`
- `exception/GlobalExceptionHandler.java`
- `templates/error/business.html`

Tests:
- test de retour d'erreur metier sur inscription.

Resultat attendu:
- message d'erreur lisible cote UI.

## Chapitre 7 - Dockerisation

Contexte:
- Besoin d'un lancement reproductible complet.

Objectifs:
- Containeriser l'app.
- Orchestrer app + MySQL + Redis.

Code a produire:
- `Dockerfile`
- `docker-compose.yml`

Tests:
- demarrage compose + verification endpoints.

Resultat attendu:
- stack complete disponible avec une commande.

## Chapitre 8 - Validation finale

Contexte:
- Livrer un resultat fiable et verifiable.

Objectifs:
- Executer les tests unitaires et integration.
- Verifier build complet.

Checklist finale:
- `mvn clean test` vert
- integration (containers) verte si Docker actif
- `docker compose up --build` fonctionnel
