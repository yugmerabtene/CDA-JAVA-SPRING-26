# Chapitre 00 - Prerequis et socle

## Objectif

Poser un projet Spring Boot qui compile immediatement et qui sert de base a tout le reste du cours.

Dans ce chapitre, on ne construit pas encore la logique metier.
On met en place le socle technique qui permettra d'empiler les briques suivantes sans casser le projet.

## Ordre d'implementation

1. verifier les outils locaux
2. creer le `pom.xml`
3. creer la classe principale Spring Boot
4. ajouter un premier test de fumee
5. verifier que le projet compile

## Prerequis

- Java 17
- Docker
- de preference Maven 3.9+, mais le cours peut aussi etre suivi uniquement avec le conteneur Maven

Cette liste est volontairement courte.
Le but du chapitre n'est pas d'ouvrir un chantier d'installation complexe, mais de verifier qu'on dispose bien du minimum pour suivre tout le reste du parcours.

Commandes utiles:

```bash
java -version
docker version
docker compose version
```

Ces commandes ne modifient rien dans le projet.
Elles servent uniquement a confirmer que l'environnement de travail est pret avant de commencer a construire l'application.

## Fichier 1 - `pom.xml`

On commence par le fichier de build complet du projet.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

    <groupId>com.cda</groupId>
    <artifactId>cda-java</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>cda-java</name>
    <description>Spring MVC app with MySQL, Redis, Docker and tests</description>

    <properties>
        <java.version>17</java.version>
        <testcontainers.version>1.20.4</testcontainers.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mysql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                        <include>**/*IT.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Pourquoi ce fichier est pose des le depart:
- il declare deja toutes les briques du projet final
- il nous evite de refaire le build a chaque chapitre
- il garantit que les chapitres suivants sont alignes sur le vrai depot

Ce choix peut sembler ambitieux des le debut, mais il simplifie en realite tout le parcours.
Au lieu de faire evoluer en permanence le build lui-meme, on fait surtout evoluer le code de l'application.
Le socle technique est ainsi pose une bonne fois pour toutes.

### Lecture detaillee de `pom.xml`

1. `modelVersion` indique a Maven le modele de descripteur utilise.
2. Le bloc `parent` rattache le projet a Spring Boot.
3. `spring-boot-starter-parent` aligne un grand nombre de versions automatiquement.
4. `groupId`, `artifactId` et `version` identifient techniquement le projet.
5. `java.version` fixe Java 17 comme cible de compilation.
6. `testcontainers.version` centralise la version des dependances Testcontainers.
7. `spring-boot-starter-web` apporte Spring MVC, Tomcat embarque et le support HTTP.
8. `spring-boot-starter-thymeleaf` apporte le moteur de templates HTML serveur.
9. `spring-boot-starter-security` apporte la pile d'authentification et d'autorisation.
10. `spring-boot-starter-data-jpa` apporte JPA, Hibernate et Spring Data.
11. `spring-boot-starter-data-redis` apporte le client Redis.
12. `spring-boot-starter-cache` active l'abstraction de cache Spring.
13. `spring-boot-starter-validation` apporte Bean Validation pour les DTO.
14. `flyway-core` et `flyway-mysql` servent aux migrations SQL.
15. `mysql-connector-j` permet la connexion JDBC vers MySQL.
16. `spring-boot-starter-test` fournit JUnit, AssertJ, Mockito et l'outillage de test Spring.
17. `spring-security-test` ajoute les helpers de test pour la securite, comme `csrf()`.
18. Les dependances Testcontainers permettent de lancer de vrais services de test en conteneurs.
19. Le bloc `dependencyManagement` importe le BOM Testcontainers pour garder des versions coherentes.
20. Le plugin `spring-boot-maven-plugin` sert au packaging et au lancement Spring Boot.
21. Le plugin Surefire est configure pour inclure les tests `*Test.java` et `*IT.java`.
22. Ce dernier point est essentiel pour que les tests unitaires et les tests d'integration soient executes dans le meme projet.

Une fois ce fichier compris, le reste du chapitre devient plus facile a lire.
Le projet a maintenant une definition de build claire.
Il faut donc lui donner un vrai point d'entree applicatif.

## Fichier 2 - `src/main/java/com/cda/cdajava/CdaJavaApplication.java`

```java
package com.cda.cdajava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CdaJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdaJavaApplication.class, args);
    }
}
```

Cette classe suffit pour demarrer l'application Spring Boot.

A ce stade, elle ne porte encore aucune logique applicative.
Son seul role est d'offrir un point d'entree stable au projet.

Dans un cours progressif, cette classe joue un role presque symbolique.
Elle marque le moment ou le projet cesse d'etre un simple dossier de fichiers pour devenir une application Java executable.

### Lecture detaillee de `CdaJavaApplication.java`

1. La ligne `package com.cda.cdajava;` place la classe dans le package racine du projet.
2. `SpringApplication` est l'utilitaire Spring Boot qui demarre le contexte.
3. `@SpringBootApplication` regroupe plusieurs annotations Spring importantes.
4. Cette annotation active notamment l'auto-configuration et le scan des composants.
5. La classe `CdaJavaApplication` est la classe d'entree du programme.
6. La methode `main` est le point d'entree Java standard.
7. `SpringApplication.run(...)` lance l'application complete.
8. A partir de cet appel, Spring charge les configurations, les composants et le serveur web embarque.

Avec cette classe, l'application devient executable.
Mais pour construire un projet serieusement, on veut aussi une toute premiere base de verification.
C'est le role du test de fumee qui suit.

## Fichier 3 - `src/test/java/com/cda/cdajava/CdaJavaApplicationTests.java`

```java
package com.cda.cdajava;

import org.junit.jupiter.api.Test;

class CdaJavaApplicationTests {

    @Test
    void smokeTest() {
    }
}
```

Ce test est volontairement minimal.
Pour le premier chapitre, on veut juste disposer d'un point d'ancrage de test.

Dans un vrai parcours de construction, ce premier test sert a repondre a une question simple:
"est-ce que mon projet de base tient debout avant que j'ajoute la base de donnees, la securite, Redis et les vues ?"

### Lecture detaillee de `CdaJavaApplicationTests.java`

1. La ligne `package` garde le test dans le meme espace logique que l'application.
2. `@Test` indique a JUnit qu'il s'agit d'une methode de test.
3. La classe de test est volontairement minimaliste.
4. La methode `smokeTest()` est vide, ce qui est assume ici.
5. Son interet n'est pas encore de tester un comportement metier.
6. Son interet est d'offrir une base de depart dans l'arborescence de tests.
7. Dans un vrai projet, ce genre de test de fumee est souvent la premiere marche du filet de securite.

Le chapitre se ferme donc avec une idee tres simple mais fondamentale.
Avant de parler base de donnees, securite, cache ou dockerisation, on s'assure d'abord que le socle du projet existe vraiment et qu'il peut etre verifie.

## Validation

Depuis un environnement sans Maven local:

```bash
docker run --rm \
  -v "$(pwd)":/workspace \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -q -DskipTests compile
```

Cette commande compile le projet dans un conteneur Maven standard.
Elle permet de verifier le socle sans dependre d'une installation Maven locale.

## Resultat attendu

- le projet compile
- le socle Spring Boot est pret
- on peut attaquer la configuration applicative au chapitre suivant

## Ce Que Ce Chapitre Apporte Au Suivant

Le chapitre suivant ne part plus d'un projet vide.
Il part d'une application Spring Boot saine, compilable et suffisamment stable pour accueillir la configuration MySQL, Redis et Flyway.
