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

## Resultat attendu

- les erreurs metier peuvent etre remontees proprement a l'interface
- l'application ne retombe pas sur une erreur HTML technique brute pour ces cas attendus
