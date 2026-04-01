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
