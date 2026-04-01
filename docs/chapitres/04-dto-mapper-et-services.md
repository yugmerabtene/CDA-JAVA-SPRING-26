# Chapitre 04 - DTO, mapper et services

## Objectif

Ajouter la logique metier d'inscription en s'appuyant sur:
- des DTO pour l'entree et la sortie
- un mapper pour convertir les objets
- des services pour centraliser les regles metier

## Ordre d'implementation

1. creer les DTO
2. creer le mapper
3. creer les interfaces de service
4. creer `AuthServiceImpl`
5. ecrire le test unitaire du service d'inscription

## Fichier 1 - `src/main/java/com/cda/cdajava/dto/RegisterRequestDto.java`

```java
package com.cda.cdajava.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequestDto {

    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

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
}
```

Ce premier DTO est la porte d'entree du formulaire d'inscription.
Toutes les contraintes de forme les plus immediates sont posees ici, avant meme que la logique metier du service ne s'executent.

## Fichier 2 - `src/main/java/com/cda/cdajava/dto/ProfileDto.java`

```java
package com.cda.cdajava.dto;

import java.io.Serializable;

public class ProfileDto implements Serializable {

    private String username;
    private String email;
    private String firstName;
    private String lastName;

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
}
```

Ce DTO sert a sortir des donnees vers la vue et vers le cache.
Le fait qu'il soit plus petit que l'entite `User` est volontaire: on ne transporte que les donnees utiles au profil.

## Fichier 3 - `src/main/java/com/cda/cdajava/dto/UpdateProfileDto.java`

```java
package com.cda.cdajava.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateProfileDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

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
}
```

La mise a jour du profil repose sur un DTO dedie.
On n'utilise pas `RegisterRequestDto`, car les besoins fonctionnels ne sont pas les memes.

## Fichier 4 - `src/main/java/com/cda/cdajava/mapper/UserMapper.java`

```java
package com.cda.cdajava.mapper;

import com.cda.cdajava.dto.ProfileDto;
import com.cda.cdajava.dto.RegisterRequestDto;
import com.cda.cdajava.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User fromRegisterDto(RegisterRequestDto dto) {
        // Le mapper copie uniquement les donnees metier simples.
        // Le hash du mot de passe et l'affectation des roles restent dans le service.
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        return user;
    }

    public ProfileDto toProfileDto(User user) {
        // Ce DTO limite les donnees exposees a la vue et au cache du profil.
        ProfileDto dto = new ProfileDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }
}
```

Le mapper apparait ici pour eviter de disperser dans les services des affectations de champs purement mecaniques.
Il garde un role simple: transformer sans prendre de decision metier complexe.

## Fichier 5 - `src/main/java/com/cda/cdajava/service/AuthService.java`

```java
package com.cda.cdajava.service;

import com.cda.cdajava.dto.RegisterRequestDto;

public interface AuthService {

    void register(RegisterRequestDto requestDto);
}
```

Cette interface pose deja une frontiere claire entre le controleur et l'implementation metier.

## Fichier 6 - `src/main/java/com/cda/cdajava/service/UserService.java`

```java
package com.cda.cdajava.service;

import com.cda.cdajava.dto.ProfileDto;
import com.cda.cdajava.dto.UpdateProfileDto;

public interface UserService {

    ProfileDto getProfile(String username);

    void updateProfile(String username, UpdateProfileDto updateProfileDto);
}
```

Le profil est deja pense en deux usages distincts:
- lecture du profil
- mise a jour du profil

## Fichier 7 - `src/main/java/com/cda/cdajava/service/impl/AuthServiceImpl.java`

```java
package com.cda.cdajava.service.impl;

import com.cda.cdajava.dao.RoleDao;
import com.cda.cdajava.dao.UserDao;
import com.cda.cdajava.dto.RegisterRequestDto;
import com.cda.cdajava.exception.BusinessException;
import com.cda.cdajava.mapper.UserMapper;
import com.cda.cdajava.model.Role;
import com.cda.cdajava.model.RoleName;
import com.cda.cdajava.model.User;
import com.cda.cdajava.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserDao userDao, RoleDao roleDao, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(RegisterRequestDto requestDto) {
        // 1) Verification metier avant ecriture SQL.
        // On bloque ici les doublons pour retourner un message clair a l'utilisateur.
        if (userDao.existsByUsername(requestDto.getUsername())) {
            throw new BusinessException("Nom d'utilisateur deja utilise");
        }
        if (userDao.existsByEmail(requestDto.getEmail())) {
            throw new BusinessException("Email deja utilise");
        }

        // 2) Chargement du role par defaut depuis la base.
        // Le role est gere en base pour rester flexible (ajout de roles futurs).
        Role role = roleDao.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BusinessException("Role par defaut introuvable"));

        // 3) Mapping DTO -> Entity puis hash du mot de passe.
        // On ne persiste jamais le mot de passe en clair.
        User user = userMapper.fromRegisterDto(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRoles(Set.of(role));

        // 4) Ecriture unique, transactionnelle.
        userDao.save(user);
    }
}
```

Ce fichier est le coeur metier de l'inscription.
Il orchestre tous les elements poses avant lui:
- DAO
- enum de roles
- mapper
- validation metier
- encodeur de mot de passe

## Fichier 8 - `src/test/java/com/cda/cdajava/service/AuthServiceImplTest.java`

```java
package com.cda.cdajava.service;

import com.cda.cdajava.dao.RoleDao;
import com.cda.cdajava.dao.UserDao;
import com.cda.cdajava.dto.RegisterRequestDto;
import com.cda.cdajava.exception.BusinessException;
import com.cda.cdajava.mapper.UserMapper;
import com.cda.cdajava.model.Role;
import com.cda.cdajava.model.RoleName;
import com.cda.cdajava.model.User;
import com.cda.cdajava.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserDao userDao;
    @Mock
    private RoleDao roleDao;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userDao, roleDao, userMapper, passwordEncoder);
    }

    @Test
    void shouldRegisterUserWhenDataIsValid() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("alice");
        request.setEmail("alice@mail.com");
        request.setPassword("secret123");
        request.setFirstName("Alice");
        request.setLastName("Doe");

        Role role = new Role();
        role.setName(RoleName.ROLE_USER);
        User mapped = new User();

        when(userDao.existsByUsername("alice")).thenReturn(false);
        when(userDao.existsByEmail("alice@mail.com")).thenReturn(false);
        when(roleDao.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(userMapper.fromRegisterDto(request)).thenReturn(mapped);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
        assertThat(captor.getValue().getRoles()).extracting(Role::getName).contains(RoleName.ROLE_USER);
    }

    @Test
    void shouldFailWhenUsernameAlreadyExists() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("alice");
        when(userDao.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("utilise");
    }
}
```

Le test vient a la fin du chapitre, une fois la logique en place.
Il verrouille le comportement voulu avant que l'on ouvre la couche web au chapitre suivant.

## Validation

```bash
docker run --rm \
  -v "$(pwd)":/workspace \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dtest=AuthServiceImplTest test
```

## Resultat attendu

- l'inscription fonctionne cote metier
- les doublons sont bloques
- le mot de passe est hash avant sauvegarde
