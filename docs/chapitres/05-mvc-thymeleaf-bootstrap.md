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

### Lecture detaillee de `HomeController.java`

1. `@Controller` indique qu'il s'agit d'un controleur MVC.
2. `@GetMapping("/")` associe la methode a la route racine.
3. La methode `home()` retourne simplement le nom de template `index`.
4. Cette classe ne contient volontairement aucune logique metier.

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

### Lecture detaillee de `AuthController.java`

1. `AuthController` injecte `AuthService` et non l'implementation concrete.
2. `registerPage(Model model)` prepare le DTO vide `registerRequest` pour Thymeleaf.
3. `return "auth/register";` demande le rendu du template `templates/auth/register.html`.
4. La methode `register(...)` traite la soumission du formulaire.
5. `@Valid` declenche Bean Validation sur `RegisterRequestDto`.
6. `BindingResult` recupere les erreurs de validation de formulaire.
7. Si `bindingResult.hasErrors()` est vrai, on reaffiche directement la page d'inscription.
8. Sinon, le controleur appelle `authService.register(request)`.
9. Le `try/catch` intercepte une `BusinessException` venant de la couche metier.
10. `model.addAttribute("errorMessage", ex.getMessage())` remonte l'erreur dans la vue.
11. En cas de succes, la methode redirige vers `/login?registered`.
12. `loginPage()` retourne simplement le template de connexion.

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

### Lecture detaillee de `ProfileController.java`

1. Le controleur injecte `UserService`.
2. `profile(Model model, Authentication authentication)` gere l'affichage de la page profil.
3. `authentication.getName()` recupere le username de l'utilisateur connecte.
4. `userService.getProfile(...)` charge les donnees du profil sous forme de DTO.
5. Un `UpdateProfileDto` est ensuite construit pour pre-remplir le formulaire de modification.
6. `model.addAttribute("profile", profile)` injecte les donnees d'affichage.
7. `model.addAttribute("updateProfile", form)` injecte l'objet de formulaire.
8. `return "profile/profile";` rend le template profil.
9. La methode `updateProfile(...)` traite la soumission du formulaire profil.
10. `@Valid` declenche Bean Validation sur `UpdateProfileDto`.
11. En cas d'erreur, le profil est recharge pour que la page reste complete.
12. En cas de succes, `userService.updateProfile(...)` applique la modification.
13. La redirection `/profile?updated` permet d'afficher un message de succes.

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

### Lecture detaillee de `index.html`

1. Le document declare `xmlns:th` pour utiliser Thymeleaf.
2. Le `<title>` nomme la page d'accueil.
3. Bootstrap est charge via CDN.
4. `th:href="@{/css/app.css}"` charge la feuille CSS du projet.
5. Le `<main>` encadre le contenu principal.
6. Le bloc `hero-panel` sert de panneau visuel central.
7. Les deux liens `@{/register}` et `@{/login}` orientent vers les parcours publics.

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

### Lecture detaillee de `register.html`

1. Le template reprend Bootstrap et la feuille CSS commune.
2. `th:if="${errorMessage}"` affiche l'erreur metier si elle existe.
3. `th:action="@{/register}"` pointe vers le POST du controleur.
4. `th:object="${registerRequest}"` relie le formulaire au DTO.
5. Chaque `th:field="*{...}"` relie un champ HTML a une propriete du DTO.
6. Chaque `th:errors="*{...}"` affiche les erreurs de Bean Validation associees.
7. Le bouton final soumet le formulaire d'inscription.

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

### Lecture detaillee de `login.html`

1. Le template charge Bootstrap et le CSS du projet.
2. `th:if="${param.registered}"` affiche un message de succes apres inscription.
3. `th:if="${param.error}"` affiche un message en cas d'echec d'authentification.
4. Le formulaire poste vers `@{/login}`.
5. Les champs `username` et `password` utilisent les noms attendus par Spring Security.
6. Le bouton soumet la tentative de connexion.

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

### Lecture detaillee de `profile.html`

1. Le haut de page affiche les informations de lecture du profil.
2. `th:text="${profile.username}"` et `th:text="${profile.email}"` lisent les valeurs du DTO `profile`.
3. `th:if="${param.updated}"` affiche un message apres redirection de succes.
4. Le second bloc contient le formulaire de modification.
5. `th:object="${updateProfile}"` relie le formulaire au DTO de mise a jour.
6. `th:field="*{firstName}"` et `th:field="*{lastName}"` lient les champs au DTO.
7. `th:errors` affiche les erreurs eventuelles.
8. Le formulaire de logout poste vers `@{/logout}` pour deconnexion Spring Security.

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

### Lecture detaillee de `app.css`

1. `:root` definit quelques variables CSS reutilisables.
2. `body` applique un fond degrade et une hauteur minimale pleine page.
3. `color: #f0f4f8;` fixe la couleur de texte principale sur le fond sombre.
4. `.hero-panel` stylise la carte principale de la page d'accueil.
5. `.card` redonne un fond clair aux cartes de formulaire et de profil.

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

### Lecture detaillee de `AuthControllerWebMvcTest.java`

1. `@WebMvcTest(controllers = AuthController.class)` charge seulement la couche MVC du controleur cible.
2. `@Import(SecurityConfig.class)` ajoute la configuration de securite necessaire au test web.
3. `@MockBean private AuthService authService;` remplace le vrai service par un mock Spring.
4. Le premier test verifie simplement que `GET /register` retourne `200 OK`.
5. Le second test simule un vrai POST de formulaire sur `/register`.
6. `.with(csrf())` ajoute le token CSRF attendu par Spring Security.
7. Les `.param(...)` remplissent les champs du formulaire.
8. Le test attend une redirection vers `/login?registered`.

## Resultat attendu

- le parcours web est en place
- l'utilisateur peut s'inscrire, se connecter et ouvrir son profil
- l'etape suivante pourra maintenant proteger ces routes avec Spring Security
