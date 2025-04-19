package br.com.marcielli.BancoM.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.entity.AuthenticationResponse;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class AuthenticationController {

	 private final AuthenticationService authService;
	 

	    public AuthenticationController(AuthenticationService authService) {
	        this.authService = authService;
	    }


	    @PostMapping("/register")
	    public ResponseEntity<AuthenticationResponse> register(
	            @RequestBody User request
	            ) {
	    	
	    
	        return ResponseEntity.ok(authService.register(request));
	    }

	    @PostMapping("/login")
	    public ResponseEntity<AuthenticationResponse> login(
	            @RequestBody User request
	    ) {
	        return ResponseEntity.ok(authService.authenticate(request));
	    }

	    @PostMapping("/refresh_token")
	    public ResponseEntity refreshToken(
	            HttpServletRequest request,
	            HttpServletResponse response
	    ) {
	        return authService.refreshToken(request, response);
	    }
	    
}
