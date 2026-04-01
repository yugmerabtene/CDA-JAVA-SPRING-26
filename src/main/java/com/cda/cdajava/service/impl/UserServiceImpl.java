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
