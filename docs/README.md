# Syllabus projet pas a pas

Ce document est la porte d'entree du cours.
Il explique ce que tu vas construire, dans quel ordre, avec quelle methode de travail, et comment utiliser les chapitres sans te perdre.

## Ce Que Tu Vas Construire

Le projet final est une application Java Spring Boot complete avec:
- une interface web serveur-side en Spring MVC et Thymeleaf
- une gestion utilisateur avec inscription, connexion et profil
- une base MySQL versionnee avec Flyway
- un cache Redis sur la lecture du profil
- une securite Spring Security en form-login
- une architecture multicouche claire: MVC, Service, Repository, DAO, DTO
- une execution complete via Docker Compose
- des tests unitaires, MVC et d'integration

En fin de parcours, tu n'auras pas seulement une suite de morceaux de code.
Tu auras une application complete, demarrable, testable et documentee de facon pedagogique.

## Comment Utiliser Ce Cours

Le principe de lecture est simple:
- comprendre la brique a construire
- ouvrir le chapitre correspondant
- suivre les fichiers dans l'ordre de construction
- lire le code complet puis l'explication qui se trouve juste dessous
- executer la validation du chapitre
- ne passer au chapitre suivant que lorsque le resultat attendu est atteint

La logique du cours repose sur une progression cumulative.
Chaque chapitre ajoute une vraie brique au projet final.
Rien n'est theorique ou deconnecte du depot: le cours suit le code reel du projet.

## Ce Que Contiennent Les Chapitres

Chaque chapitre contient:
- un objectif clair
- l'ordre chronologique d'implementation
- les fichiers complets a creer ou a comprendre
- des explications detaillees juste apres les blocs de code
- une validation
- un resultat attendu
- une transition vers le chapitre suivant

L'idee n'est pas seulement de montrer du code.
L'idee est d'expliquer pourquoi ce code arrive a ce moment du parcours, comment il s'insere dans l'architecture, et ce qu'il prepare pour la suite.

## Ordre De Lecture Recommande

1. ce `README.md`
2. `chapitres/README.md`
3. puis les chapitres dans l'ordre numerique

Si tu suis le cours pour apprendre:
- lis d'abord l'objectif du chapitre
- lis ensuite les fichiers dans l'ordre propose
- lis l'explication detaillee apres chaque bloc
- termine par la validation

Si tu suis le cours pour reviser:
- lis ce syllabus
- va directement au chapitre qui t'interesse
- utilise les sections de transition pour replacer la brique dans le projet global

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

Cet ordre est essentiel.
Le cours n'est pas organise par theme abstrait, mais par dependances reelles entre les briques.
On ne parle pas de securite avant d'avoir des utilisateurs, et on ne parle pas de cache avant d'avoir une vraie lecture de profil a optimiser.

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

Si tu gardes ce schema en tete pendant toute la lecture, beaucoup de decisions du projet deviennent plus claires.
Chaque chapitre ne construit pas seulement une fonctionnalite; il remplit aussi une place precise dans cette architecture.

## Regles du parcours

- ne pas sauter de chapitre
- ne pas changer l'architecture en cours de route
- toujours valider une brique avant de passer a la suivante
- garder le code et le cours strictement alignes

Ces regles ont un vrai interet pedagogique.
Si on saute une brique ou si on modifie l'architecture en plein milieu, on perd le benefice du parcours progressif.
Le but du cours est justement de montrer comment une application complete se construit proprement, et pas seulement comment elle apparait a la fin.

## Comment Lire Le Code Dans Ce Cours

Le cours a ete ecrit pour que le code soit lu de maniere active.

Quand tu rencontres un bloc de code:
- lis d'abord le fichier complet pour voir sa structure generale
- lis ensuite le texte juste en dessous
- identifie le role de la classe ou du template dans l'architecture
- repere ce que ce fichier apporte de nouveau par rapport au chapitre precedent

Cette methode est particulierement importante sur les chapitres centraux:
- `04` pour la logique metier
- `05` pour la couche MVC
- `06` pour la securite
- `10` pour la validation finale

## Ce Que Le Parcours T'Apprend Vraiment

Au-dela du code lui-meme, ce cours apprend aussi une methode de construction de projet:
- poser un socle avant d'empiler les dependances
- versionner la base avant de mapper les entites
- separer le web, le metier et la persistence
- valider le metier avant l'interface
- proteger ensuite l'application
- optimiser ensuite avec le cache
- finir par une execution conteneurisee et des tests proches du reel

Autrement dit, le cours n'enseigne pas seulement Spring Boot.
Il enseigne aussi un ordre de travail propre pour construire une application complete.

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
