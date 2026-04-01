# Chapitre 02 - Modele JPA et enum roles

## Objectif

Creer les entites qui correspondent exactement au schema SQL pose au chapitre precedent.

## Ordre d'implementation

1. creer l'enum `RoleName`
2. creer l'entite `Role`
3. creer l'entite `User`
4. verifier que le mapping colle au schema Flyway

Cet ordre n'est pas arbitraire.
On commence par le vocabulaire le plus simple, puis on passe a l'entite la plus petite, puis a l'entite centrale du projet.

## Fichier 1 - `src/main/java/com/cda/cdajava/model/RoleName.java`

```java
package com.cda.cdajava.model;

public enum RoleName {
    ROLE_USER,
    ROLE_ADMIN
}
```

Le projet manipule les noms de role avec un enum pour eviter les fautes de frappe dans toute l'application.

Cette decision arrive tres tot volontairement, parce qu'elle impacte ensuite:
- le repository des roles
- le service d'inscription
- la conversion en authorities Spring Security

Ce chapitre est donc plus qu'un simple chapitre "JPA".
Il fixe le vocabulaire metier du projet et prepare deja plusieurs couches qui arriveront apres lui.

### Lecture detaillee de `RoleName.java`

1. Le `package` le place dans le domaine metier.
2. `public enum RoleName` declare un type ferme de valeurs possibles.
3. `ROLE_USER` represente le role standard des utilisateurs inscrits.
4. `ROLE_ADMIN` existe deja dans le domaine pour stabiliser le vocabulaire metier.
5. Le fait de centraliser ces valeurs empeche les fautes de frappe dispersees dans le code.

Une fois l'enum pose, on peut lui donner un support persistant concret.
La classe `Role` sera justement la traduction JPA de cette notion de role metier.

## Fichier 2 - `src/main/java/com/cda/cdajava/model/Role.java`

```java
package com.cda.cdajava.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // On persiste l'enum en texte pour garder une base lisible et stable.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }
}
```

Cette entite reste volontairement simple.
Elle ne contient qu'un identifiant et un nom de role, ce qui est suffisant pour le besoin du projet.

### Lecture detaillee de `Role.java`

1. `@Entity` indique a JPA qu'il s'agit d'une entite persistante.
2. `@Table(name = "roles")` la relie explicitement a la table SQL `roles`.
3. `@Id` marque le champ identifiant primaire.
4. `@GeneratedValue(strategy = GenerationType.IDENTITY)` s'aligne sur l'auto-increment MySQL.
5. Le champ `id` est purement technique.
6. `@Enumerated(EnumType.STRING)` impose le stockage du nom d'enum en texte.
7. `@Column(nullable = false, unique = true)` reproduit les contraintes SQL.
8. Le champ `name` est typiquement la vraie valeur metier de cette entite.
9. Les getters et setters permettent a JPA et au reste de l'application d'acceder aux donnees.

Le role etant pose, il manque maintenant l'entite autour de laquelle va tourner l'application: l'utilisateur.
C'est elle qui fera le lien entre inscription, profil, securite et cache.

## Fichier 3 - `src/main/java/com/cda/cdajava/model/User.java`

```java
package com.cda.cdajava.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Chargement immediat des roles: Spring Security en a besoin au moment du login.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    public void onCreate() {
        // Premier insert: on initialise les deux timestamps avec la meme valeur.
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        // Chaque modification rafraichit la date de mise a jour automatiquement.
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
```

Cette entite est la plus importante du domaine.
Elle relie la table `users`, la table d'association `user_roles`, la securite et le futur affichage du profil.

On voit ici apparaitre le veritable coeur metier du projet.
Beaucoup de chapitres suivants vont tourner autour de cette entite `User`, soit pour la creer, soit pour l'afficher, soit pour l'authentifier, soit pour la mettre en cache.

### Lecture detaillee de `User.java`

1. `@Entity` et `@Table(name = "users")` relient la classe a la table `users`.
2. `id` est la cle primaire technique de l'utilisateur.
3. `username` est obligatoire et unique.
4. `email` est egalement obligatoire et unique.
5. `password` est obligatoire car l'authentification en depend.
6. `firstName` et `lastName` servent a l'identite fonctionnelle du profil.
7. `createdAt` et `updatedAt` sont relies aux colonnes SQL correspondantes.
8. `@ManyToMany(fetch = FetchType.EAGER)` declare la relation avec les roles.
9. `@JoinTable(name = "user_roles", ...)` explique a JPA comment passer par la table d'association.
10. `joinColumns = @JoinColumn(name = "user_id")` relie l'utilisateur courant.
11. `inverseJoinColumns = @JoinColumn(name = "role_id")` relie les roles associes.
12. `Set<Role> roles = new HashSet<>()` initialise la collection et evite les doublons.
13. `@PrePersist` prepare les timestamps avant le premier insert.
14. `LocalDateTime now = LocalDateTime.now()` prend l'instant courant une seule fois.
15. `createdAt` et `updatedAt` recoivent la meme valeur au debut de vie de l'entite.
16. `@PreUpdate` rafraichit `updatedAt` avant chaque modification.
17. Les getters et setters rendent l'entite manipulable par JPA, les services et les mappers.

Quand ce fichier est compris, on voit mieux la force du chapitre.
Le projet possede maintenant un domaine Java qui colle a la base SQL et qui porte deja les besoins des chapitres a venir.

## Resultat attendu

- le domaine Java correspond au schema SQL
- `Role.name` s'appuie sur l'enum `RoleName`
- `User` porte deja la relation avec les roles

## Ce Que Ce Chapitre Apporte Au Suivant

Le chapitre suivant peut maintenant brancher l'acces aux donnees.
Les repositories et DAO auront des entites claires a manipuler, deja alignees sur le schema SQL et sur le vocabulaire metier du projet.
