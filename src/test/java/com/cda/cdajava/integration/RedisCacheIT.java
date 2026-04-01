package com.cda.cdajava.integration;

import com.cda.cdajava.dto.ProfileDto;
import com.cda.cdajava.dto.RegisterRequestDto;
import com.cda.cdajava.dto.UpdateProfileDto;
import com.cda.cdajava.service.AuthService;
import com.cda.cdajava.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisCacheIT extends AbstractContainerIT {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCacheAndEvictProfile() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("cache-user");
        request.setEmail("cache-user@mail.com");
        request.setPassword("password123");
        request.setFirstName("Cache");
        request.setLastName("User");
        authService.register(request);

        ProfileDto profile = userService.getProfile("cache-user");
        assertThat(profile.getFirstName()).isEqualTo("Cache");

        Cache cache = cacheManager.getCache("profiles");
        assertThat(cache).isNotNull();
        assertThat(cache.get("cache-user")).isNotNull();

        UpdateProfileDto update = new UpdateProfileDto();
        update.setFirstName("Updated");
        update.setLastName("User");
        userService.updateProfile("cache-user", update);

        assertThat(cache.get("cache-user")).isNull();
    }
}
