package com.cda.cdajava.controller;

import com.cda.cdajava.dto.RegisterRequestDto;
import com.cda.cdajava.exception.BusinessException;
import com.cda.cdajava.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        // On prepare un DTO vide pour que Thymeleaf puisse binder le formulaire.
        model.addAttribute("registerRequest", new RegisterRequestDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequestDto request,
            BindingResult bindingResult,
            Model model) {

        // Les erreurs de validation restent dans la couche web: on reaffiche le formulaire.
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            // La logique metier d'inscription reste centralisee dans le service.
            authService.register(request);
        } catch (BusinessException ex) {
            // Erreur metier attendue: on retourne sur la meme page avec un message lisible.
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
}
