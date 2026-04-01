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

Commandes utiles:

```bash
java -version
docker version
docker compose version
```

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

## Validation

Depuis un environnement sans Maven local:

```bash
docker run --rm \
  -v "$(pwd)":/workspace \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -q -DskipTests compile
```

## Resultat attendu

- le projet compile
- le socle Spring Boot est pret
- on peut attaquer la configuration applicative au chapitre suivant
