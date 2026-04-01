# CDA Java - Spring MVC multicouche

Application Java Spring Boot avec:
- inscription / connexion / profil
- architecture MVC + Service + Repository + DAO + DTO
- MySQL pour les donnees relationnelles
- Redis pour le cache
- Docker Compose pour la stack complete
- tests unitaires, MVC et integration

Application finale: `http://localhost:8080`

## Prerequis

- Java 17+
- Maven 3.9+
- Docker et Docker Compose

## Demarrage rapide en local

Si MySQL et Redis tournent deja sur `localhost` avec les ports par defaut:

```bash
mvn clean test
mvn spring-boot:run
```

## Demarrage complet avec Docker

```bash
docker compose up -d --build
```

Services exposes:
- App: `http://localhost:8080`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

## Strategie de test

- unitaires: logique metier avec `Mockito`
- Web MVC: controleur d'authentification avec `MockMvc`
- integration: MySQL + Redis reels avec `Testcontainers`

## Parcours pedagogique

Documents a lire dans cet ordre:

1. `docs/README.md`
2. `docs/chapitres/README.md`

Role de chaque document:
- `docs/README.md`: syllabus public et point d'entree du parcours
- `docs/chapitres/README.md`: cours decoupe en fichiers par chapitre
