package br.com.marcielli.bancom.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class HomeController {

    @RequestMapping("/login")
    public ModelAndView showLoginPage() {
        // Altere "Login" para "login" (o nome exato do arquivo, case sensitive)
        return new ModelAndView("login");
    }





    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin(){
        return "Hello, Admin";
    }





}
