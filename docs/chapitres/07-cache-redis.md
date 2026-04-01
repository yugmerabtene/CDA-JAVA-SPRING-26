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

## Resultat attendu

- le profil est lu depuis MySQL au premier appel
- les appels suivants reutilisent Redis
- une mise a jour de profil supprime l'entree de cache correspondante
