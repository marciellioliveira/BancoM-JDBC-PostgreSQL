package br.com.marcielli.BancoM.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.marcielli.BancoM.dto.UserRegisterDTO;


@Controller
public class FormController {

    @GetMapping("/cadastro")
    public String showForm(Model model) {
    	model.addAttribute("user", new UserRegisterDTO());
        return "register";
    }
}