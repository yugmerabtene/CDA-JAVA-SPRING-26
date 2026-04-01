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

## Validation

```bash
docker run --rm \
  -v "$(pwd)":/workspace \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn clean test
```

## Resultat attendu

- les tests unitaires passent
- les tests MVC passent
- les tests d'integration passent avec de vrais conteneurs
- le projet arrive bien a son etat final verifie
