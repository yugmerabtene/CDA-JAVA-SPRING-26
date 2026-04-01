package com.cda.cdajava.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Page d'entree publique: aucune logique metier, on retourne simplement la vue.
        return "index";
    }
}
