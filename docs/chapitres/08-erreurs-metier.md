# Chapitre 08 - Erreurs metier

## Objectif

Centraliser les erreurs fonctionnelles attendues et afficher une page propre cote MVC.

## Ordre d'implementation

1. creer une exception metier dediee
2. creer un handler global MVC
3. creer la page d'erreur metier

## Fichier 1 - `src/main/java/com/cda/cdajava/exception/BusinessException.java`

```java
package com.cda.cdajava.exception;

// Exception reservee aux erreurs fonctionnelles attendues par le metier.
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
```

Cette exception est utilisee pour les erreurs fonctionnelles attendues.
Elle se distingue volontairement des erreurs techniques imprevues.

Ce choix est important pedagogiquement et architecturalement.
Toutes les erreurs ne se valent pas.

Dans ce projet, on distingue:
- les erreurs metier pre visibles, comme un username deja utilise
- les erreurs techniques non prevues, comme un probleme d'infrastructure

`BusinessException` sert justement a nommer proprement la premiere categorie.

### Lecture detaillee de `BusinessException.java`

1. La classe herite de `RuntimeException`.
2. Cela permet de la lever sans declaration obligatoire dans les signatures de methode.
3. Le constructeur prend simplement un message lisible.
4. Le projet utilise cette exception pour remonter des erreurs metier attendues vers la couche web.

Le fait qu'elle soit volontairement simple est un avantage ici.
Le but n'est pas de construire une hierarchie d'exceptions complexe, mais de disposer d'un signal clair pour les erreurs fonctionnelles attendues.

## Fichier 2 - `src/main/java/com/cda/cdajava/exception/GlobalExceptionHandler.java`

```java
package com.cda.cdajava.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/business";
    }
}
```

Avec ce handler, l'application MVC garde une sortie propre lorsque le metier leve une erreur connue.

Ce fichier joue un role de tampon entre la couche metier et la couche de presentation.
La couche metier leve une exception.
La couche web n'a pas besoin de savoir comment la transformer en page HTML.
Le `ControllerAdvice` s'en charge de maniere centralisee.

### Lecture detaillee de `GlobalExceptionHandler.java`

1. `@ControllerAdvice` applique ce handler a l'ensemble des controleurs MVC.
2. `@ExceptionHandler(BusinessException.class)` cible specifiquement les erreurs metier.
3. La methode `handleBusiness(...)` recoit l'exception levee.
4. `model.addAttribute("errorMessage", ex.getMessage())` rend le message disponible dans la vue.
5. `return "error/business";` affiche le template d'erreur metier.

Cette centralisation evite de reimplementer le meme traitement dans plusieurs controleurs.
Le projet gagne donc a la fois:
- en lisibilite
- en coherence
- en maintenance

## Fichier 3 - `src/main/resources/templates/error/business.html`

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Erreur metier</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<main class="container py-5">
    <div class="alert alert-danger" th:text="${errorMessage}">Erreur metier</div>
    <a class="btn btn-primary" th:href="@{/}">Retour accueil</a>
</main>
</body>
</html>
```

La page d'erreur reste volontairement simple.
Le but ici est la lisibilite fonctionnelle, pas la sophistication visuelle.

Cette simplicite est un bon choix pour une page d'erreur metier.
L'utilisateur doit surtout comprendre ce qui se passe et pouvoir revenir facilement au reste de l'application.

### Lecture detaillee de `business.html`

1. Le template charge Bootstrap pour garder une presentation propre.
2. `th:text="${errorMessage}"` affiche le message fourni par le handler global.
3. Le lien de retour `@{/}` permet de revenir facilement a l'accueil.
4. Ce template sert de sortie commune a plusieurs erreurs metier possibles.

Avec ce chapitre, l'application gagne en maturite fonctionnelle.
Elle n'affiche plus seulement des pages de succes; elle gere aussi proprement des cas d'erreur attendus.

## Resultat attendu

- les erreurs metier peuvent etre remontees proprement a l'interface
- l'application ne retombe pas sur une erreur HTML technique brute pour ces cas attendus

## Ce Que Ce Chapitre Apporte Au Suivant

Le chapitre suivant peut maintenant emballer toute l'application dans une stack Docker sans perdre en lisibilite fonctionnelle.
Les comportements metier principaux, y compris les erreurs attendues, sont deja stabilises avant la mise en conteneurs.
