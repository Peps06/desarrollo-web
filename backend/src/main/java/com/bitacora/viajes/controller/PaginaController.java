package com.bitacora.viajes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PaginaController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/viajes";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @GetMapping("/viajes")
    public String listarViajes() {
        return "viajes/lista";
    }

    @GetMapping("/viajes/nuevo")
    public String nuevoViaje() {
        return "viajes/nuevo";
    }

    @GetMapping("/viajes/{id}")
    public String detalleViaje(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("viajeId", id);
        return "viajes/detalle";
    }
}