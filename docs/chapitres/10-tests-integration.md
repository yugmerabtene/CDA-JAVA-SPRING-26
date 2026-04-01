# Chapitre 10 - Tests d'integration

## Objectif

Verifier l'application finale avec de vrais services MySQL et Redis demarres a la demande par Testcontainers.

## Ordre d'implementation

1. creer une base commune Testcontainers
2. verifier le flux reel d'inscription
3. verifier le comportement reel du cache Redis

## Fichier 1 - `src/test/java/com/cda/cdajava/integration/AbstractContainerIT.java`

```java
package com.cda.cdajava.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractContainerIT {

    @Container
    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0.37")
            .withDatabaseName("cda_java_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:7.4-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }
}
```

Cette classe factorise tout le socle d'integration.
Les tests reels du projet pourront ainsi se concentrer sur leur comportement sans recopier la configuration des conteneurs.

Ce choix est tres important dans un vrai projet.
Si chaque test recreait lui-meme MySQL, Redis et toute la configuration Spring correspondante, les tests deviendraient vite verbeux et difficiles a maintenir.

En mettant ce socle commun dans une classe abstraite, on garde:
- une configuration unique
- des tests plus courts
- un comportement de test plus coherent sur tout le projet

### Lecture detaillee de `AbstractContainerIT.java`

1. `@Testcontainers(disabledWithoutDocker = true)` active Testcontainers pour cette hierarchie de tests.
2. `MYSQL_CONTAINER` demarre un vrai MySQL de test.
3. `withDatabaseName`, `withUsername` et `withPassword` prepareront la base temporaire.
4. `REDIS_CONTAINER` demarre un vrai Redis de test.
5. `withExposedPorts(6379)` expose le port Redis du conteneur.
6. `@DynamicPropertySource` permet d'injecter dynamiquement les valeurs runtime dans Spring.
7. `spring.datasource.url`, `username` et `password` sont remplaces par ceux du conteneur MySQL.
8. `spring.data.redis.host` et `spring.data.redis.port` sont remplaces par ceux du conteneur Redis.
9. Cette classe est la base commune des tests d'integration reels du projet.

Il faut bien comprendre la difference avec un simple profil `test` statique.
Le profil `test` donne une base de configuration, mais ici ce sont les conteneurs qui fournissent les vraies valeurs runtime.

Autrement dit:
- `application-test.yml` prepare le terrain
- `AbstractContainerIT` branche Spring sur les vrais services lances pour le test

Cela permet d'executer des tests beaucoup plus proches de la realite.

## Fichier 2 - `src/test/java/com/cda/cdajava/integration/AuthFlowIT.java`

```java
package com.cda.cdajava.integration;

import com.cda.cdajava.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIT extends AbstractContainerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterNewUserAndPersistInMysql() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "bob")
                        .param("email", "bob@mail.com")
                        .param("password", "password123")
                        .param("firstName", "Bob")
                        .param("lastName", "Martin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        assertThat(userRepository.findByUsername("bob")).isPresent();
        assertThat(userRepository.findByUsername("bob").orElseThrow().getPassword())
                .isNotEqualTo("password123");
    }
}
```

Ce test rejoue le flux reel d'inscription au niveau web, jusqu'a la persistence en base.
Il verifie donc plusieurs couches en une seule fois.

Pedagogiquement, c'est un test tres riche.
Il ne teste pas uniquement un service ou un controleur isole.
Il rejoue un parcours presque complet de l'application:
- requete HTTP MVC
- validation et controleur
- service metier
- persistence MySQL
- securisation du mot de passe

### Lecture detaillee de `AuthFlowIT.java`

1. `@SpringBootTest` demarre l'application complete pour le test.
2. `@AutoConfigureMockMvc` permet de piloter la couche web sans navigateur reel.
3. `@ActiveProfiles("test")` active le profil de test.
4. La classe herite de `AbstractContainerIT`, donc MySQL et Redis sont disponibles.
5. `MockMvc` est injecte pour rejouer une requete HTTP MVC.
6. `UserRepository` est injecte pour verifier la persistence finale.
7. Le test envoie un `POST /register` avec un token CSRF.
8. Les `.param(...)` remplissent le formulaire d'inscription.
9. Le test attend une redirection vers `/login?registered`.
10. `userRepository.findByUsername("bob")` verifie que l'utilisateur existe bien en base.
11. L'assertion finale verifie que le mot de passe stocke n'est pas le mot de passe brut.

Ce test est tres utile pour comprendre la valeur d'un test d'integration.
Le test unitaire de `AuthServiceImpl` avait deja verifie la logique metier en isolation.
Ici, on verifie que cette logique fonctionne aussi quand elle traverse les vraies couches de l'application.

En d'autres termes, ce test repond a la question:
"Quand un vrai formulaire HTTP arrive, est-ce que l'utilisateur est vraiment cree correctement dans une vraie base MySQL ?"

## Fichier 3 - `src/test/java/com/cda/cdajava/integration/RedisCacheIT.java`

```java
package com.cda.cdajava.integration;

import com.cda.cdajava.dto.ProfileDto;
import com.cda.cdajava.dto.RegisterRequestDto;
import com.cda.cdajava.dto.UpdateProfileDto;
import com.cda.cdajava.service.AuthService;
import com.cda.cdajava.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisCacheIT extends AbstractContainerIT {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCacheAndEvictProfile() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("cache-user");
        request.setEmail("cache-user@mail.com");
        request.setPassword("password123");
        request.setFirstName("Cache");
        request.setLastName("User");
        authService.register(request);

        ProfileDto profile = userService.getProfile("cache-user");
        assertThat(profile.getFirstName()).isEqualTo("Cache");

        Cache cache = cacheManager.getCache("profiles");
        assertThat(cache).isNotNull();
        assertThat(cache.get("cache-user")).isNotNull();

        UpdateProfileDto update = new UpdateProfileDto();
        update.setFirstName("Updated");
        update.setLastName("User");
        userService.updateProfile("cache-user", update);

        assertThat(cache.get("cache-user")).isNull();
    }
}
```

Ce second test d'integration se concentre sur le cache Redis.
Il prouve que l'application finale n'est pas seulement correcte fonctionnellement, mais aussi coherente sur son comportement de cache.

Ce test est complementaire du precedent.
Le premier valide surtout le flux d'inscription et la persistence relationnelle.
Celui-ci valide un comportement transverse plus technique:
- lecture service
- cache Spring
- backend Redis
- eviction apres mise a jour

### Lecture detaillee de `RedisCacheIT.java`

1. `@SpringBootTest` charge l'application complete.
2. `@ActiveProfiles("test")` active le profil de test.
3. `AuthService` est utilise pour inscrire un utilisateur de facon metier.
4. `UserService` est ensuite utilise pour lire et modifier le profil.
5. `CacheManager` permet d'observer l'etat du cache applique par Spring.
6. Le test construit un `RegisterRequestDto` et appelle `authService.register(request)`.
7. `userService.getProfile("cache-user")` declenche la lecture et la mise en cache du profil.
8. `cacheManager.getCache("profiles")` recupere le cache cible.
9. `assertThat(cache.get("cache-user")).isNotNull()` prouve que l'entree est bien en cache.
10. Un `UpdateProfileDto` est ensuite construit.
11. `userService.updateProfile(...)` modifie le profil et evince l'entree de cache.
12. `assertThat(cache.get("cache-user")).isNull()` verifie l'eviction.

Ce test est precieux, car il montre que le cache ne doit pas seulement exister.
Il doit aussi rester correct.

Un cache mal invalide peut produire des bugs subtils:
- un profil affiche des donnees anciennes
- une modification semble ne pas avoir marche
- l'utilisateur voit une valeur differente de celle en base

Ici, le test garantit que la mise a jour supprime bien l'entree de cache devenue obsolete.

## Validation

```bash
docker run --rm \
  -v "$(pwd)":/workspace \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn clean test
```

Cette commande execute tous les tests dans un conteneur Maven.
Le partage du socket Docker est necessaire pour que Testcontainers puisse lancer MySQL et Redis pendant les tests d'integration.

Le choix d'une commande unique `mvn clean test` est pedagogiquement tres fort.
Il montre qu'a la fin du parcours, tout le projet peut etre verifie d'un seul coup:
- tests unitaires
- tests MVC
- tests d'integration

On passe ainsi d'un projet en construction progressive a un projet valide de facon globale et reproductible.

## Resultat attendu

- les tests unitaires passent
- les tests MVC passent
- les tests d'integration passent avec de vrais conteneurs
- le projet arrive bien a son etat final verifie

Quand ce chapitre est termine, le cours boucle la demonstration complete.
On ne s'arrete pas a "le code existe" ou "la page s'affiche".
On prouve aussi que l'application finale fonctionne reellement dans une configuration proche du monde reel.
