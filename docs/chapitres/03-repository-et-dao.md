# Chapitre 03 - Repository et DAO

## Objectif

Brancher le domaine sur Spring Data JPA et poser la couche DAO qui sera utilisee par les services.

## Ordre d'implementation

1. creer les repositories JPA
2. creer les interfaces DAO
3. creer les implementations DAO
4. preparer les services pour le chapitre suivant

Ce chapitre suit une logique de profondeur technique.
On commence au plus pres de Spring Data, puis on remonte vers une couche plus metier qui sera ensuite consommee par les services.

## Fichier 1 - `src/main/java/com/cda/cdajava/repository/UserRepository.java`

```java
package com.cda.cdajava.repository;

import com.cda.cdajava.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
```

Avec ce repository, Spring Data genere directement les operations necessaires sans ecriture SQL manuelle dans le code Java.

### Lecture detaillee de `UserRepository.java`

1. `JpaRepository<User, Long>` donne deja les operations CRUD de base.
2. Le type `User` indique l'entite geree.
3. Le type `Long` indique le type de cle primaire.
4. `findByUsername` sera utilise par la securite et la lecture du profil.
5. `existsByUsername` servira a bloquer les doublons a l'inscription.
6. `existsByEmail` sert au meme objectif sur l'email.

Le repository montre ce que Spring Data sait faire tres simplement.
Mais pour garder une architecture plus lisible dans le reste du projet, on va maintenant encapsuler cet acces dans une couche DAO.

## Fichier 2 - `src/main/java/com/cda/cdajava/repository/RoleRepository.java`

```java
package com.cda.cdajava.repository;

import com.cda.cdajava.model.Role;
import com.cda.cdajava.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
```

Le point important ici est la signature `findByName(RoleName name)`.
On reste coherent avec l'enum metier au lieu de repasser sur une chaine de caracteres.

### Lecture detaillee de `RoleRepository.java`

1. `JpaRepository<Role, Long>` apporte les operations de base sur l'entite `Role`.
2. `findByName(RoleName name)` repose sur l'enum metier.
3. Le retour `Optional<Role>` force l'appelant a traiter explicitement le cas d'absence.

Les repositories sont maintenant poses.
Ils savent parler a la base, mais ils restent des outils assez proches de Spring Data.
Les DAO vont maintenant offrir une facade de persistence plus adaptee au reste de l'application.

## Fichier 3 - `src/main/java/com/cda/cdajava/dao/UserDao.java`

```java
package com.cda.cdajava.dao;

import com.cda.cdajava.model.User;

import java.util.Optional;

public interface UserDao {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);
}
```

Cette interface correspond deja exactement aux besoins des services qui vont suivre.
On ne cree pas un DAO generique trop large: on expose seulement ce que le projet utilise vraiment.

Ce point est pedagogiquement important.
On ne construit pas une architecture "pour faire joli" ou pour ajouter des couches artificielles.
Chaque methode exposee ici correspond a un besoin reel des services a venir.

### Lecture detaillee de `UserDao.java`

1. `findByUsername` est la lecture principale du projet.
2. `existsByUsername` et `existsByEmail` exposent des verifications metier simples.
3. `save` permet d'enregistrer aussi bien un nouvel utilisateur qu'une mise a jour.
4. L'interface formalise ce dont les services ont besoin, sans exposer tout le repository.

Cette interface montre bien l'idee de filtrage de responsabilite.
On n'expose pas tout ce que JPA sait faire, seulement ce que la logique du projet utilisera vraiment.

## Fichier 4 - `src/main/java/com/cda/cdajava/dao/RoleDao.java`

```java
package com.cda.cdajava.dao;

import com.cda.cdajava.model.Role;
import com.cda.cdajava.model.RoleName;

import java.util.Optional;

public interface RoleDao {

    Optional<Role> findByName(RoleName name);
}
```

Le DAO de role est tres petit, ce qui est normal.
Dans l'application finale, son besoin principal est de retrouver le role par defaut au moment de l'inscription.

### Lecture detaillee de `RoleDao.java`

1. L'interface ne contient qu'une methode, ce qui est volontaire.
2. `findByName(RoleName name)` correspond exactement au besoin du service d'inscription.
3. Comme pour le repository, le retour est un `Optional<Role>`.

Il reste maintenant a relier ces interfaces DAO aux repositories concrets.
Les implementations qui suivent sont simples, mais elles sont importantes pour faire vivre l'architecture choisie.

## Fichier 5 - `src/main/java/com/cda/cdajava/dao/impl/UserDaoImpl.java`

```java
package com.cda.cdajava.dao.impl;

import com.cda.cdajava.dao.UserDao;
import com.cda.cdajava.model.User;
import com.cda.cdajava.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    public UserDaoImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
```

Ici, l'implementation se contente de deleguer au repository.
Cette couche devient utile pedagogiquement, car elle permet au service de dependre d'une couche d'acces aux donnees explicite.

### Lecture detaillee de `UserDaoImpl.java`

1. `@Repository` declare ce composant comme une couche d'acces aux donnees.
2. `private final UserRepository userRepository;` injecte le repository sous-jacent.
3. Le constructeur impose l'injection de cette dependance.
4. `findByUsername` ne fait que deleguer au repository.
5. `existsByUsername` et `existsByEmail` deleguent aussi.
6. `save` centralise l'enregistrement via Spring Data.
7. La classe ne contient pas de logique metier, ce qui est un bon signe.

Cette implementation peut sembler tres fine, mais c'est justement ce qui est interessant.
Elle prouve que la couche DAO n'est pas faite pour dupliquer de la logique, mais pour structurer l'acces aux donnees.

## Fichier 6 - `src/main/java/com/cda/cdajava/dao/impl/RoleDaoImpl.java`

```java
package com.cda.cdajava.dao.impl;

import com.cda.cdajava.dao.RoleDao;
import com.cda.cdajava.model.Role;
import com.cda.cdajava.model.RoleName;
import com.cda.cdajava.repository.RoleRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleDaoImpl implements RoleDao {

    private final RoleRepository roleRepository;

    public RoleDaoImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findByName(RoleName name) {
        return roleRepository.findByName(name);
    }
}
```

Avec cette derniere piece, l'acces aux donnees est pret.
Le chapitre suivant peut maintenant se concentrer sur le vrai comportement metier: inscription, hash du mot de passe, affectation du role et profil utilisateur.

On arrive donc a la fin de la preparation technique de la persistence.
La suite va enfin montrer comment ces briques servent un vrai cas d'usage utilisateur.

### Lecture detaillee de `RoleDaoImpl.java`

1. `@Repository` place cette classe dans la couche persistence.
2. `private final RoleRepository roleRepository;` stocke le repository injecte.
3. Le constructeur realise l'injection par constructeur.
4. `findByName` delegue a `roleRepository.findByName(name)`.
5. Comme pour `UserDaoImpl`, l'interet principal est structurel: preparer une couche DAO claire pour le service.

Avec cette derniere classe, toute la chaine d'acces aux donnees est maintenant en place.
Le chapitre suivant pourra enfin s'interesser a la vraie logique metier, en reutilisant cette structure sans avoir a se soucier du detail de la persistence.
## Resultat attendu

- les services disposent maintenant d'un acces propre aux donnees
- la base est accessible a travers `Repository` puis `DAO`
- le chapitre suivant peut enfin poser la logique metier d'inscription

## Ce Que Ce Chapitre Apporte Au Suivant

Le chapitre suivant pourra se concentrer sur la logique metier pure.
Les services n'auront plus besoin de se preocuper de la facon dont on accede a la base, car cette responsabilite est maintenant deja structuree.
