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
