# Syllabus projet pas a pas

Ce document est le point d'entree public du cours.

Le principe de lecture est le suivant:
- comprendre la brique a construire
- ouvrir le chapitre correspondant
- suivre les fichiers dans l'ordre de construction
- lire le code complet et son explication detaillee
- valider la brique avant de passer a la suivante

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

1. ce `README.md`
2. `chapitres/README.md`
3. puis les chapitres dans l'ordre numerique

Structure du cours:
- un chapitre = une brique fonctionnelle ou technique
- le code est presente avec les fichiers complets
- les explications sont placees juste apres les blocs de code
- chaque chapitre se termine par une validation et un resultat attendu

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

Sous ce bloc, on lit simplement l'ordre logique de construction du projet.
On commence par le socle, on ajoute ensuite la persistence et le domaine, puis la logique metier, l'interface, la securite, le cache, la dockerisation et enfin la validation complete.

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

Ce schema resume l'architecture multicouche du projet.
Le navigateur parle aux controleurs MVC, les controleurs deleguent aux services, les services utilisent les DAO et repositories pour la base, le cache Redis pour certaines lectures, et les mappers pour convertir entre entites et DTO.

## Regles du parcours

- ne pas sauter de chapitre
- ne pas changer l'architecture en cours de route
- toujours valider une brique avant de passer a la suivante
- garder le code et le cours strictement alignes

## Parcours detaille

### Chapitre 00 - Prerequis et socle

Objectif:
- poser un projet Spring Boot compilable et testable

Contenu principal:
- `pom.xml`
- classe principale Spring Boot
- premier test de fumee

Resultat attendu:
- le projet compile et peut servir de base au reste du cours

### Chapitre 01 - Configuration et Flyway

Objectif:
- configurer MySQL, Redis et les migrations SQL

Contenu principal:
- `application.yml`
- `application-test.yml`
- `V1__create_auth_tables.sql`
- `V2__seed_roles.sql`

Resultat attendu:
- le schema est versionne et pret pour les entites JPA

### Chapitre 02 - Modele JPA et enum roles

Objectif:
- creer le domaine Java qui correspond au schema SQL

Contenu principal:
- `RoleName`
- `Role`
- `User`

Resultat attendu:
- le modele metier est pose et aligne sur la base

### Chapitre 03 - Repository et DAO

Objectif:
- brancher le domaine sur Spring Data JPA et structurer l'acces aux donnees

Contenu principal:
- `UserRepository`
- `RoleRepository`
- `UserDao`, `RoleDao`
- `UserDaoImpl`, `RoleDaoImpl`

Resultat attendu:
- les services peuvent acceder proprement a la persistence

### Chapitre 04 - DTO, mapper et services

Objectif:
- implementer la logique metier d'inscription et de profil

Contenu principal:
- DTO d'entree et de sortie
- `UserMapper`
- `AuthServiceImpl`
- test unitaire du service d'inscription

Resultat attendu:
- l'inscription hash le mot de passe et affecte le role par defaut

### Chapitre 05 - MVC, Thymeleaf et Bootstrap

Objectif:
- construire l'interface web complete de l'application

Contenu principal:
- `HomeController`, `AuthController`, `ProfileController`
- templates `index`, `register`, `login`, `profile`
- feuille CSS
- test MVC du controleur d'inscription

Resultat attendu:
- le premier parcours utilisateur complet fonctionne cote web

### Chapitre 06 - Spring Security

Objectif:
- proteger les routes privees et brancher le login sur la base

Contenu principal:
- `SecurityConfig`
- `CustomUserDetailsService`

Resultat attendu:
- `/profile` est protege et l'authentification fonctionne

### Chapitre 07 - Cache Redis

Objectif:
- accelerer la lecture du profil avec un cache Redis coherent

Contenu principal:
- `CacheConfig`
- `UserServiceImpl` avec `@Cacheable` et `@CacheEvict`

Resultat attendu:
- la lecture du profil est mise en cache et l'eviction fonctionne a la mise a jour

### Chapitre 08 - Erreurs metier

Objectif:
- centraliser les erreurs fonctionnelles attendues

Contenu principal:
- `BusinessException`
- `GlobalExceptionHandler`
- page `error/business.html`

Resultat attendu:
- l'interface affiche des erreurs lisibles pour les cas metier connus

### Chapitre 09 - Dockerisation

Objectif:
- lancer toute la stack avec une seule commande

Contenu principal:
- `Dockerfile`
- `docker-compose.yml`

Resultat attendu:
- MySQL, Redis et l'application demarrent ensemble

### Chapitre 10 - Tests d'integration

Objectif:
- valider l'application finale avec de vrais services en conteneurs

Contenu principal:
- `AbstractContainerIT`
- `AuthFlowIT`
- `RedisCacheIT`

Resultat attendu:
- l'application finale est verifiee en conditions proches du reel
