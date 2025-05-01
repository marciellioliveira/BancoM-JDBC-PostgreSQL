package br.com.marcielli.bancom.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class HomeController {

	@GetMapping
	public String home() {
		return "Hello, World";
	}
	
	@GetMapping("/user")
	public String user() {
		return "Hello, User";
	}
	
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin(){
        return "Hello, Admin";
    }



//  @RequestMapping("/login")
//  public ModelAndView showLoginPage() {       
//      return new ModelAndView("login");
//  }




}
