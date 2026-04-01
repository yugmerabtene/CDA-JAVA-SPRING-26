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

### Lecture detaillee de `RegisterRequestDto.java`

1. Le `package` place le DTO dans la couche des contrats d'echange.
2. `@NotBlank` sur `username` interdit une valeur vide ou blanche.
3. `@Size(min = 3, max = 100)` encadre la longueur du nom d'utilisateur.
4. `@Email` sur `email` impose un format d'adresse coherent.
5. `@Size(min = 8, max = 72)` sur `password` impose une borne minimale et une borne compatible avec BCrypt.
6. `firstName` et `lastName` sont obligatoires.
7. Les getters et setters permettent a Spring MVC et Thymeleaf de binder les champs du formulaire.

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

### Lecture detaillee de `ProfileDto.java`

1. `implements Serializable` prepare cet objet a un usage simple dans le cache.
2. `username`, `email`, `firstName` et `lastName` suffisent a l'affichage du profil.
3. Il n'y a pas de champ `password`, ce qui evite de remonter des donnees sensibles vers la vue.
4. Les getters et setters servent au mapper et au rendu Thymeleaf.

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

### Lecture detaillee de `UpdateProfileDto.java`

1. Ce DTO ne contient que `firstName` et `lastName`.
2. Il n'y a ni `username`, ni `email`, ni `password`, car la page profil ne modifie pas ces champs.
3. `@NotBlank` impose que le formulaire de mise a jour reste complet.
4. La structure du DTO suit strictement le besoin de la fonctionnalite.

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

### Lecture detaillee de `UserMapper.java`

1. `@Component` permet a Spring d'injecter cette classe la ou elle est necessaire.
2. `fromRegisterDto` cree une nouvelle entite `User`.
3. `setUsername`, `setEmail`, `setFirstName` et `setLastName` copient les champs simples du DTO.
4. Le mot de passe n'est pas affecte ici, ce qui est volontaire.
5. Les roles ne sont pas non plus affectes ici.
6. `toProfileDto` cree ensuite un objet de sortie adapte a la vue.
7. Cette seconde methode copie uniquement les informations utiles au profil.

## Fichier 5 - `src/main/java/com/cda/cdajava/service/AuthService.java`

```java
package com.cda.cdajava.service;

import com.cda.cdajava.dto.RegisterRequestDto;

public interface AuthService {

    void register(RegisterRequestDto requestDto);
}
```

Cette interface pose deja une frontiere claire entre le controleur et l'implementation metier.

### Lecture detaillee de `AuthService.java`

1. L'interface expose une seule methode `register`.
2. Cette methode prend un `RegisterRequestDto`, donc un contrat d'entree de couche web.
3. Le controleur ne depend ainsi pas directement de `AuthServiceImpl`.

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

### Lecture detaillee de `UserService.java`

1. `getProfile(String username)` exprime clairement le besoin de lecture.
2. `updateProfile(String username, UpdateProfileDto updateProfileDto)` exprime clairement le besoin de modification.
3. L'interface montre que la couche service travaille deja avec des DTO et non avec l'entite brute cote presentation.

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

### Lecture detaillee de `AuthServiceImpl.java`

1. `@Service` marque la classe comme service metier Spring.
2. Les champs `userDao`, `roleDao`, `userMapper` et `passwordEncoder` sont les dependances du flux d'inscription.
3. Le constructeur impose leur injection.
4. `@Transactional` sur `register` garantit que l'inscription reste atomique.
5. `userDao.existsByUsername(...)` verifie le doublon de login.
6. En cas de doublon, une `BusinessException` lisible est levee.
7. `userDao.existsByEmail(...)` verifie ensuite le doublon d'email.
8. `roleDao.findByName(RoleName.ROLE_USER)` recupere le role par defaut en base.
9. `orElseThrow(...)` transforme l'absence du role en erreur metier claire.
10. `userMapper.fromRegisterDto(requestDto)` convertit le DTO en entite.
11. `passwordEncoder.encode(...)` transforme le mot de passe en hash BCrypt.
12. `user.setRoles(Set.of(role))` rattache le role standard a l'utilisateur.
13. `userDao.save(user)` persiste l'utilisateur complet.
14. Toute la logique metier importante du flux est concentree ici.

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

### Lecture detaillee de `AuthServiceImplTest.java`

1. `@ExtendWith(MockitoExtension.class)` active Mockito dans JUnit 5.
2. `@Mock` cree des doubles pour toutes les dependances du service.
3. `setUp()` instancie `AuthServiceImpl` avec ces mocks.
4. Le premier test construit un `RegisterRequestDto` complet.
5. Il prepare ensuite un `Role` et un `User` simules.
6. `when(...)` definit le comportement attendu des mocks.
7. `authService.register(request)` execute la logique reelle du service.
8. `ArgumentCaptor<User>` recupere l'utilisateur envoye au `save`.
9. L'assertion sur le mot de passe verifie le hash.
10. L'assertion sur les roles verifie que `ROLE_USER` est bien rattache.
11. Le second test verifie le cas d'erreur sur username deja existant.
12. `assertThatThrownBy(...)` verifie qu'une `BusinessException` est bien levee.

## Validation

```bash
docker run --rm \
  -v "$(pwd)":/workspace \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -Dtest=AuthServiceImplTest test
```

Cette commande execute uniquement le test unitaire du service d'inscription.
Elle permet de valider la logique metier avant de passer a la couche web complete.

## Resultat attendu

- l'inscription fonctionne cote metier
- les doublons sont bloques
- le mot de passe est hash avant sauvegarde
