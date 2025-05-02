package br.com.marcielli.bancom.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.bancom.dto.LoginRequestDTO;
import br.com.marcielli.bancom.dto.LoginResponseDTO;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.service.JwtService;
import br.com.marcielli.bancom.service.UserClienteService;


@RestController
public class AuthController {
	
	private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserClienteService userClienteService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserClienteService userClienteService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userClienteService = userClienteService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDTO loginRequest) {
    	System.out.println("Tentando login para: " + loginRequest.username());
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.username(),
                    loginRequest.password()
                )
            );
            
            System.out.println("Autenticação bem-sucedida para: " + loginRequest.username());

            // Carregar o usuário do banco pelo username
            User user = userClienteService.findByUsername(loginRequest.username());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado");
            }

            // Gerar token usando JwtService
            String token = jwtService.gerarToken(user);

            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }

}
