# Chapitre 07 - Cache Redis

## Objectif

Ajouter le cache sur la lecture du profil et invalider ce cache quand le profil est modifie.

Cette etape fait evoluer la couche service sans changer l'interface publique de l'application.

## Ordre d'implementation

1. activer le cache Spring
2. completer `UserServiceImpl`
3. verifier le comportement avec un test d'integration

## Fichier 1 - `src/main/java/com/cda/cdajava/config/CacheConfig.java`

```java
package com.cda.cdajava.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Configuration minimale: Spring cree l'infrastructure de cache a partir de application.yml.
}
```

Le fait que ce fichier soit minuscule est normal.
Le gros du comportement vient des annotations de cache placees ensuite dans le service.

Ce chapitre montre une idee importante en Spring.
Parfois, une petite classe de configuration active un comportement tres riche dans toute l'application.
Ici, `@EnableCaching` suffit a ouvrir toute l'infrastructure de cache Spring.

### Lecture detaillee de `CacheConfig.java`

1. `@Configuration` declare une classe de configuration Spring.
2. `@EnableCaching` active l'infrastructure de cache Spring pour tout le projet.
3. La classe n'a pas besoin d'autre contenu car la configuration technique principale vient de `application.yml`.

## Fichier 2 - `src/main/java/com/cda/cdajava/service/impl/UserServiceImpl.java`

```java
package com.cda.cdajava.service.impl;

import com.cda.cdajava.dao.UserDao;
import com.cda.cdajava.dto.ProfileDto;
import com.cda.cdajava.dto.UpdateProfileDto;
import com.cda.cdajava.exception.BusinessException;
import com.cda.cdajava.mapper.UserMapper;
import com.cda.cdajava.model.User;
import com.cda.cdajava.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    public UserServiceImpl(UserDao userDao, UserMapper userMapper) {
        this.userDao = userDao;
        this.userMapper = userMapper;
    }

    @Override
    @Cacheable(value = "profiles", key = "#username")
    public ProfileDto getProfile(String username) {
        // Read-through cache: premier appel -> SQL, appels suivants -> Redis.
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));
        return userMapper.toProfileDto(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "profiles", key = "#username")
    public void updateProfile(String username, UpdateProfileDto updateProfileDto) {
        // Mise a jour transactionnelle puis eviction cache pour coherence immediate.
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));
        user.setFirstName(updateProfileDto.getFirstName());
        user.setLastName(updateProfileDto.getLastName());
        userDao.save(user);
    }
}
```

Ici, `UserServiceImpl` devient la vraie facade de lecture et mise a jour du profil.
Le controleur ne change pas, mais le service devient plus intelligent.

On voit bien ici l'interet d'avoir pose les couches proprement auparavant.
Le controleur n'a pas besoin d'etre recrit pour beneficier du cache: toute l'evolution reste localisee dans la couche service.

C'est exactement le type d'evolution qu'on recherche dans une bonne architecture.
On ameliore le comportement applicatif sans toucher aux routes, sans toucher aux vues et sans casser le contrat du service.

Le cache est donc ajoute comme une optimisation transversale, mais proprement localisee.

### Lecture detaillee de `UserServiceImpl.java`

1. `@Service` identifie la classe comme service metier injectable.
2. `userDao` sert a lire et sauvegarder l'utilisateur.
3. `userMapper` convertit l'entite `User` en `ProfileDto`.
4. `@Cacheable(value = "profiles", key = "#username")` demande a Spring de mettre en cache le resultat de `getProfile`.
5. Au premier appel pour un username donne, la methode s'execute vraiment.
6. `userDao.findByUsername(username)` charge l'utilisateur depuis MySQL.
7. En cas d'absence, une `BusinessException` est levee.
8. `userMapper.toProfileDto(user)` transforme l'entite en DTO de sortie.
9. Aux appels suivants avec la meme cle, Spring peut renvoyer la valeur depuis Redis.
10. `@Transactional` protege la mise a jour du profil.
11. `@CacheEvict(value = "profiles", key = "#username")` supprime l'entree de cache apres modification.
12. `updateProfile` recharge l'utilisateur en base.
13. Les champs `firstName` et `lastName` sont mis a jour.
14. `userDao.save(user)` persiste les nouvelles valeurs.
15. Le prochain `getProfile(username)` rechargera des donnees fraiches.

Il faut bien distinguer les deux comportements complementaires:

1. `@Cacheable` optimise la lecture.
2. `@CacheEvict` preserve la coherence apres ecriture.

Si on ne gardait que `@Cacheable`, on pourrait finir avec un profil perime dans Redis.
Si on ne gardait que `@CacheEvict`, on n'aurait aucune optimisation de lecture.

Les deux annotations fonctionnent donc ensemble pour maintenir a la fois:
- la performance
- la coherence des donnees

## Resultat attendu

- le profil est lu depuis MySQL au premier appel
- les appels suivants reutilisent Redis
- une mise a jour de profil supprime l'entree de cache correspondante

Pedagogiquement, ce chapitre est tres utile parce qu'il montre qu'une application peut evoluer en qualite technique sans changer son comportement visible par l'utilisateur.
L'ecran profil reste le meme, mais derriere lui, la lecture devient plus efficace et plus scalable.

## Ce Que Ce Chapitre Apporte Au Suivant

Le chapitre suivant va se concentrer sur les cas d'erreur metier.
Comme la lecture et la mise a jour du profil sont maintenant stabilisees, on peut mieux expliquer comment remonter proprement les situations fonctionnelles problematiques vers l'interface.
