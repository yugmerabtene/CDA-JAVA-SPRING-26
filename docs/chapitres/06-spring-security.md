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

Il faut bien voir que ce fichier ne cree pas de nouvelles pages.
Il change la facon dont les pages existantes sont accessibles.
Autrement dit, il ajoute une regle de circulation devant l'application.
Certaines routes restent ouvertes, d'autres exigent une authentification.

### Lecture detaillee de `SecurityConfig.java`

1. `@Configuration` declare une classe de configuration Spring.
2. `securityFilterChain(HttpSecurity http)` construit la chaine de filtres Spring Security.
3. `authorizeHttpRequests(...)` declare les regles d'acces aux routes.
4. `requestMatchers("/", "/register", "/login", "/css/**").permitAll()` ouvre explicitement les routes publiques.
5. `anyRequest().authenticated()` protege toutes les autres routes.
6. `formLogin(...)` active l'authentification par formulaire HTML.
7. `loginPage("/login")` remplace la page standard de Spring Security par notre vue.
8. `defaultSuccessUrl("/profile", true)` redirige toujours vers le profil apres connexion.
9. `logout(...)` configure la deconnexion et son URL de retour.
10. `http.build()` retourne la chaine de filtres finale.
11. Le bean `PasswordEncoder` retourne un `BCryptPasswordEncoder`.
12. Ce meme bean sera injecte dans `AuthServiceImpl` pour hasher les mots de passe.

Ce point est essentiel pour comprendre la coherence globale du projet.
Le meme mecanisme de hash est utilise a deux moments differents:
- lors de l'inscription, pour enregistrer le mot de passe sous forme securisee
- lors de la connexion, pour comparer le mot de passe saisi avec le hash stocke

Cela veut dire que `SecurityConfig` ne sert pas seulement a proteger les routes.
Il fournit aussi une dependance centrale a toute la strategie d'authentification du projet.

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

En pratique, Spring Security sait gerer un login et un mot de passe, mais il ne connait pas notre modele metier par defaut.
Il ne sait pas ce qu'est un `User`, ni comment retrouver ses roles dans MySQL.

Cette classe joue donc le role d'adaptateur.
Elle traduit notre modele applicatif vers le format attendu par le framework de securite.

### Lecture detaillee de `CustomUserDetailsService.java`

1. `@Service` rend cette classe injectable par Spring.
2. `implements UserDetailsService` annonce que la classe sait charger un utilisateur pour Spring Security.
3. `userDao` est injecte pour lire l'utilisateur en base.
4. `loadUserByUsername(String username)` est la methode cle appelee par Spring Security.
5. `userDao.findByUsername(username)` recherche l'utilisateur du login.
6. `orElseThrow(...)` leve une `UsernameNotFoundException` si le login n'existe pas.
7. `new org.springframework.security.core.userdetails.User(...)` construit l'objet compris par Spring Security.
8. Le premier argument est le username authentifie.
9. Le second est le mot de passe hash stocke en base.
10. Le troisieme est la collection d'authorities converties a partir des roles.
11. `toAuthorities(user)` isole la conversion des roles vers le format securite.
12. `map(Role::getName)` extrait l'enum `RoleName`.
13. `map(Enum::name)` transforme l'enum en texte `ROLE_USER` ou `ROLE_ADMIN`.
14. `map(SimpleGrantedAuthority::new)` cree l'objet d'autorite attendu.
15. `collect(Collectors.toSet())` rassemble ces autorites dans un ensemble.

Le point cle ici est la chaine complete de transformation:
- la base contient un `Role`
- l'entite `Role` contient un `RoleName`
- `RoleName` devient une chaine comme `ROLE_USER`
- cette chaine devient une `GrantedAuthority`

Cette progression est tres importante pedagogiquement, car elle montre comment un concept metier du domaine devient une notion de securite exploitable par Spring.

On voit aussi que la securite ne contourne pas l'architecture.
Elle ne lit pas la base directement avec du SQL a part.
Elle passe par la couche DAO et par le modele metier deja construits dans les chapitres precedents.

## Resultat attendu

- `/register`, `/login` et `/` restent publics
- `/profile` devient protege
- la connexion repose sur les utilisateurs et les roles stockes en base

Quand ce chapitre est termine, l'application change de niveau.
Elle n'est plus seulement une application web qui affiche des pages et enregistre des utilisateurs.
Elle devient une application web avec session authentifiee, regles d'acces et reutilisation des roles du domaine.

## Ce Que Ce Chapitre Apporte Au Suivant

Le chapitre suivant pourra optimiser une fonctionnalite deja realisticamente utilisee: la lecture du profil d'un utilisateur connecte.
Le cache prendra donc place sur une route et un service deja legitimes dans le parcours de l'application.
