# Chapitre 06 - Spring Security

## Objectif

Passer d'une interface web fonctionnelle a une interface web securisee.

Dans ce chapitre:
- on protege les routes privees
- on branche le login sur les utilisateurs de la base
- on fournit le `PasswordEncoder` reutilise par l'inscription

## Ordre d'implementation

1. creer `SecurityConfig`
2. creer `CustomUserDetailsService`
3. verifier que `/profile` devient une route protegee

## Fichier 1 - `src/main/java/com/cda/cdajava/config/SecurityConfig.java`

```java
package com.cda.cdajava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Pages publiques accessibles sans session authentifiee.
                        .requestMatchers("/", "/register", "/login", "/css/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        // On remplace la page de login par notre template Thymeleaf.
                        .loginPage("/login")
                        .defaultSuccessUrl("/profile", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

Ce fichier fait basculer l'application dans un autre etat.
Avant lui, les routes MVC existent. Apres lui, elles sont filtrees et certaines deviennent protegees.

## Fichier 2 - `src/main/java/com/cda/cdajava/security/CustomUserDetailsService.java`

```java
package com.cda.cdajava.security;

import com.cda.cdajava.dao.UserDao;
import com.cda.cdajava.model.Role;
import com.cda.cdajava.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Spring Security nous donne un username; on recharge donc l'utilisateur reel depuis la base.
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouve"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                toAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> toAuthorities(User user) {
        // Les roles applicatifs (enum) deviennent ici les authorities attendues par Spring Security.
        return user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
```

Ce service est la piece qui relie directement Spring Security au modele `User` du projet.
Sans lui, la securite ne saurait pas comment charger un utilisateur depuis la base de donnees.

## Resultat attendu

- `/register`, `/login` et `/` restent publics
- `/profile` devient protege
- la connexion repose sur les utilisateurs et les roles stockes en base
