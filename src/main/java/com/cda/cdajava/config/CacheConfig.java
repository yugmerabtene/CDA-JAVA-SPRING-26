package com.cda.cdajava.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Configuration minimale: Spring cree l'infrastructure de cache a partir de application.yml.
}
