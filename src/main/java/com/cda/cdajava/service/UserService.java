package com.cda.cdajava.service;

import com.cda.cdajava.dto.ProfileDto;
import com.cda.cdajava.dto.UpdateProfileDto;

public interface UserService {

    ProfileDto getProfile(String username);

    void updateProfile(String username, UpdateProfileDto updateProfileDto);
}
