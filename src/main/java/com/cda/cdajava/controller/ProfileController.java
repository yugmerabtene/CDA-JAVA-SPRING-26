package com.cda.cdajava.controller;

import com.cda.cdajava.dto.ProfileDto;
import com.cda.cdajava.dto.UpdateProfileDto;
import com.cda.cdajava.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        ProfileDto profile = userService.getProfile(authentication.getName());

        // On separe la lecture (`profile`) de l'ecriture (`updateProfile`) pour la vue.
        UpdateProfileDto form = new UpdateProfileDto();
        form.setFirstName(profile.getFirstName());
        form.setLastName(profile.getLastName());

        model.addAttribute("profile", profile);
        model.addAttribute("updateProfile", form);
        return "profile/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("updateProfile") UpdateProfileDto updateProfile,
            BindingResult bindingResult,
            Authentication authentication,
            Model model) {

        if (bindingResult.hasErrors()) {
            // On recharge les donnees affichees pour conserver une page complete en cas d'erreur.
            model.addAttribute("profile", userService.getProfile(authentication.getName()));
            return "profile/profile";
        }

        // Le controleur transmet uniquement la demande de mise a jour au service.
        userService.updateProfile(authentication.getName(), updateProfile);
        return "redirect:/profile?updated";
    }
}
