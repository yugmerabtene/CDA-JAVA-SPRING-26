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

## Resultat attendu

- les tests unitaires passent
- les tests MVC passent
- les tests d'integration passent avec de vrais conteneurs
- le projet arrive bien a son etat final verifie
