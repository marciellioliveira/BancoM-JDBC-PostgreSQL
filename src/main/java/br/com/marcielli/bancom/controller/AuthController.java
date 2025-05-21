package br.com.marcielli.bancom.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.bancom.dto.LoginRequestDTO;
import br.com.marcielli.bancom.exception.ClienteNaoEncontradoException;
import br.com.marcielli.bancom.service.JwtService;
import br.com.marcielli.bancom.service.UserClienteService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserClienteService userClienteService;

	public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
			UserClienteService userClienteService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userClienteService = userClienteService;
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
	    System.out.println("Tentativa de login para: " + request.username());

	    try {
	        // Autenticação
	        Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(
	                request.username(),
	                request.password()
	            )
	        );

	        // Configura a autenticação no contexto de segurança
	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        // Obtém os detalhes
	        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	        String role = authentication.getAuthorities().stream()
	            .findFirst()
	            .map(GrantedAuthority::getAuthority)
	            .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
	            .orElse("USER");

	        System.err.println("Role AuthController: " + role);

	        // Verifica se a role é válida (ADMIN ou BASIC)
	        if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("BASIC")) {
	            throw new ClienteNaoEncontradoException("Usuário não tem permissão para acessar o sistema.");
	        }

	        // Gera o token
	        String token = jwtService.generateToken(userDetails.getUsername(), role);

	        // Retorna o token e a role
	        return ResponseEntity.ok(Map.of(
	            "token", token,
	            "role", role
	        ));

	    } catch (AuthenticationException e) {
	        System.out.println("Falha no login: " + e.getMessage());
	        return ResponseEntity
	            .status(HttpStatus.UNAUTHORIZED)
	            .body("Usuário ou senha inválidos.");
	    } catch (ClienteNaoEncontradoException e) {
	        System.out.println("Acesso negado: " + e.getMessage());
	        return ResponseEntity
	            .status(HttpStatus.FORBIDDEN)
	            .body(e.getMessage());
	    }
	}


//	@PostMapping("/login")
//	public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
//	    System.out.println("Tentativa de login para: " + request.username());
//
//	    try {
//	        //  Autenticação
//	        Authentication authentication = authenticationManager.authenticate(
//	            new UsernamePasswordAuthenticationToken(
//	                request.username(),
//	                request.password()
//	            )
//	        );
//
//	        //  Configura a autenticação no contexto de segurança
//	        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//	        //  Obtém os detalhes
//	        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//	        String role = authentication.getAuthorities().stream()
//	        	    .findFirst()
//	        	    .map(GrantedAuthority::getAuthority)
//	        	    .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
//	        	    .orElse("USER");
//
//	       System.err.println("Role AuthController: "+role);
////	        String role = authentication.getAuthorities().stream()
////	            .findFirst()
////	            .map(GrantedAuthority::getAuthority)
////	            .orElse("ROLE_USER");
//
//	        System.out.println("Login bem-sucedido para: " + userDetails.getUsername() + " | Role: " + role);
//
//	        //  Gera o token
//	        String token = jwtService.generateToken(userDetails.getUsername(), role);
//
//	        // Retorna o token e a role
//	        return ResponseEntity.ok(Map.of(
//	            "token", token,
//	            "role", role
//	        ));
//
//	    } catch (AuthenticationException e) {
//	        System.out.println("Falha no login: " + e.getMessage());
//	        return ResponseEntity
//	                .status(HttpStatus.UNAUTHORIZED)
//	                .body("Usuário não encontrado.");
//
//	    }
//	}

}
