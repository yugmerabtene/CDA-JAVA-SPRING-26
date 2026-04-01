# Chapitre 02 - Modele JPA et enum roles

## Objectif

Creer les entites qui correspondent exactement au schema SQL pose au chapitre precedent.

## Ordre d'implementation

1. creer l'enum `RoleName`
2. creer l'entite `Role`
3. creer l'entite `User`
4. verifier que le mapping colle au schema Flyway

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

## Resultat attendu

- le domaine Java correspond au schema SQL
- `Role.name` s'appuie sur l'enum `RoleName`
- `User` porte deja la relation avec les roles
