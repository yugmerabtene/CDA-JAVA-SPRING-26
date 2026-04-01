# Chapitre 05 - MVC, Thymeleaf et Bootstrap

## Objectif

Construire toute la partie interface serveur-side:
- accueil
- inscription
- connexion
- profil

## Ordre d'implementation

1. creer le controleur d'accueil
2. creer le controleur d'authentification
3. creer le controleur de profil
4. creer les templates Thymeleaf
5. ajouter la feuille de style
6. tester la couche MVC

## Fichier 1 - `src/main/java/com/cda/cdajava/controller/HomeController.java`

```java
package com.cda.cdajava.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Page d'entree publique: aucune logique metier, on retourne simplement la vue.
        return "index";
    }
}
```

Le controleur d'accueil est volontairement minimal pour garder une entree publique simple dans l'application.

## Fichier 2 - `src/main/java/com/cda/cdajava/controller/AuthController.java`

```java
package com.cda.cdajava.controller;

import com.cda.cdajava.dto.RegisterRequestDto;
import com.cda.cdajava.exception.BusinessException;
import com.cda.cdajava.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        // On prepare un DTO vide pour que Thymeleaf puisse binder le formulaire.
        model.addAttribute("registerRequest", new RegisterRequestDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequestDto request,
            BindingResult bindingResult,
            Model model) {

        // Les erreurs de validation restent dans la couche web: on reaffiche le formulaire.
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            // La logique metier d'inscription reste centralisee dans le service.
            authService.register(request);
        } catch (BusinessException ex) {
            // Erreur metier attendue: on retourne sur la meme page avec un message lisible.
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
}
```

Ce controleur devient la jonction entre:
- les contraintes web du formulaire
- la validation Bean Validation
- la logique metier d'inscription du service

## Fichier 3 - `src/main/java/com/cda/cdajava/controller/ProfileController.java`

```java
package com.cda.cdajava.controller;

import com.cda.cdajava.dto.ProfileDto;
import com.cda.cdajava.dto.UpdateProfileDto;
import com.cda.cdajava.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        ProfileDto profile = userService.getProfile(authentication.getName());

        // On separe la lecture (`profile`) de l'ecriture (`updateProfile`) pour la vue.
        UpdateProfileDto form = new UpdateProfileDto();
        form.setFirstName(profile.getFirstName());
        form.setLastName(profile.getLastName());

        model.addAttribute("profile", profile);
        model.addAttribute("updateProfile", form);
        return "profile/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("updateProfile") UpdateProfileDto updateProfile,
            BindingResult bindingResult,
            Authentication authentication,
            Model model) {

        if (bindingResult.hasErrors()) {
            // On recharge les donnees affichees pour conserver une page complete en cas d'erreur.
            model.addAttribute("profile", userService.getProfile(authentication.getName()));
            return "profile/profile";
        }

        // Le controleur transmet uniquement la demande de mise a jour au service.
        userService.updateProfile(authentication.getName(), updateProfile);
        return "redirect:/profile?updated";
    }
}
```

Ce controleur entrelace deja deux notions importantes du projet:
- l'utilisateur actuellement authentifie
- la lecture et la mise a jour de son profil via `UserService`

## Fichier 4 - `src/main/resources/templates/index.html`

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CDA Java - Accueil</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container py-5">
    <div class="p-5 rounded-4 hero-panel">
        <h1 class="display-5 fw-semibold">Projet Spring MVC multicouche</h1>
        <p class="lead">Inscription, connexion, profil utilisateur, MySQL, Redis et Docker.</p>
        <div class="d-flex gap-2">
            <a class="btn btn-primary" th:href="@{/register}">S'inscrire</a>
            <a class="btn btn-outline-light" th:href="@{/login}">Se connecter</a>
        </div>
    </div>
</main>
</body>
</html>
```

La page d'accueil pose le ton du projet et oriente immediatement vers les deux flux publics: inscription et connexion.

## Fichier 5 - `src/main/resources/templates/auth/register.html`

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Inscription</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-6">
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h1 class="h4 mb-3">Inscription</h1>
                    <div class="alert alert-danger" th:if="${errorMessage}" th:text="${errorMessage}"></div>
                    <form method="post" th:action="@{/register}" th:object="${registerRequest}">
                        <div class="mb-3">
                            <label class="form-label">Nom d'utilisateur</label>
                            <input class="form-control" th:field="*{username}">
                            <div class="text-danger small" th:errors="*{username}"></div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input class="form-control" th:field="*{email}">
                            <div class="text-danger small" th:errors="*{email}"></div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Mot de passe</label>
                            <input class="form-control" type="password" th:field="*{password}">
                            <div class="text-danger small" th:errors="*{password}"></div>
                        </div>
                        <div class="row g-2">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Prenom</label>
                                <input class="form-control" th:field="*{firstName}">
                                <div class="text-danger small" th:errors="*{firstName}"></div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Nom</label>
                                <input class="form-control" th:field="*{lastName}">
                                <div class="text-danger small" th:errors="*{lastName}"></div>
                            </div>
                        </div>
                        <button class="btn btn-primary w-100" type="submit">Creer le compte</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>
</body>
</html>
```

Le template d'inscription est lie au DTO `registerRequest` defini dans le controleur.
Chaque champ et chaque erreur de validation ont deja leur place.

## Fichier 6 - `src/main/resources/templates/auth/login.html`

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Connexion</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h1 class="h4 mb-3">Connexion</h1>
                    <div class="alert alert-success" th:if="${param.registered}">Inscription terminee. Connectez-vous.</div>
                    <div class="alert alert-danger" th:if="${param.error}">Identifiants invalides.</div>
                    <form method="post" th:action="@{/login}">
                        <div class="mb-3">
                            <label class="form-label" for="username">Nom d'utilisateur</label>
                            <input class="form-control" id="username" name="username" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label" for="password">Mot de passe</label>
                            <input class="form-control" id="password" name="password" type="password" required>
                        </div>
                        <button class="btn btn-primary w-100" type="submit">Se connecter</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>
</body>
</html>
```

La page de login reste simple, car l'authentification elle-meme sera prise en charge par Spring Security au chapitre suivant.

## Fichier 7 - `src/main/resources/templates/profile/profile.html`

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mon profil</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>
<body>
<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-sm mb-3">
                <div class="card-body">
                    <h1 class="h4">Mon profil</h1>
                    <p class="mb-1"><strong>Username:</strong> <span th:text="${profile.username}"></span></p>
                    <p class="mb-1"><strong>Email:</strong> <span th:text="${profile.email}"></span></p>
                    <div class="alert alert-success mt-3" th:if="${param.updated}">Profil mis a jour.</div>
                </div>
            </div>
            <div class="card shadow-sm">
                <div class="card-body">
                    <h2 class="h5 mb-3">Modifier mes informations</h2>
                    <form method="post" th:action="@{/profile}" th:object="${updateProfile}">
                        <div class="row g-2">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Prenom</label>
                                <input class="form-control" th:field="*{firstName}">
                                <div class="text-danger small" th:errors="*{firstName}"></div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Nom</label>
                                <input class="form-control" th:field="*{lastName}">
                                <div class="text-danger small" th:errors="*{lastName}"></div>
                            </div>
                        </div>
                        <button class="btn btn-primary" type="submit">Enregistrer</button>
                    </form>
                    <form class="mt-3" method="post" th:action="@{/logout}">
                        <button class="btn btn-outline-secondary" type="submit">Se deconnecter</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>
</body>
</html>
```

La page profil ferme le premier vrai parcours utilisateur du projet:
inscription, connexion, affichage du compte, modification des informations.

## Fichier 8 - `src/main/resources/static/css/app.css`

```css
:root {
    --bg-a: #102a43;
    --bg-b: #243b53;
    --panel: rgba(255, 255, 255, 0.08);
}

body {
    min-height: 100vh;
    background: radial-gradient(circle at 20% 10%, #334e68, var(--bg-a) 45%),
                linear-gradient(135deg, var(--bg-a), var(--bg-b));
    color: #f0f4f8;
}

.hero-panel {
    background: var(--panel);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #f0f4f8;
}

.card {
    background: #f8fbff;
    color: #102a43;
}
```

La feuille de style reste volontairement courte.
Le but du cours est de montrer une interface propre et utilisable sans faire deriver le projet vers un chantier frontend trop lourd.

## Fichier 9 - `src/test/java/com/cda/cdajava/controller/AuthControllerWebMvcTest.java`

```java
package com.cda.cdajava.controller;

import com.cda.cdajava.config.SecurityConfig;
import com.cda.cdajava.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void shouldDisplayRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRedirectToLoginAfterRegistration() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "alice")
                        .param("email", "alice@mail.com")
                        .param("password", "password123")
                        .param("firstName", "Alice")
                        .param("lastName", "Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }
}
```

Ce test valide la couche MVC sans demarrer toute l'application complete.
Il joue donc un role de garde-fou rapide avant le branchement complet de la securite.

## Resultat attendu

- le parcours web est en place
- l'utilisateur peut s'inscrire, se connecter et ouvrir son profil
- l'etape suivante pourra maintenant proteger ces routes avec Spring Security
