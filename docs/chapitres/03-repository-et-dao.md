# Chapitre 03 - Repository et DAO

## Objectif

Brancher le domaine sur Spring Data JPA et poser la couche DAO qui sera utilisee par les services.

## Ordre d'implementation

1. creer les repositories JPA
2. creer les interfaces DAO
3. creer les implementations DAO
4. preparer les services pour le chapitre suivant

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
## Resultat attendu

- les services disposent maintenant d'un acces propre aux donnees
- la base est accessible a travers `Repository` puis `DAO`
- le chapitre suivant peut enfin poser la logique metier d'inscription
