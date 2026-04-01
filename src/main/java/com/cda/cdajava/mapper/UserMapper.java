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
