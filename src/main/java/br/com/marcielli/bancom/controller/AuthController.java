package br.com.marcielli.bancom.controller;

import org.springframework.http.HttpHeaders;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.bancom.dto.LoginRequestDTO;
import br.com.marcielli.bancom.dto.LoginResponseDTO;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.service.JwtService;
import br.com.marcielli.bancom.service.UserClienteService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/auth")
public class AuthController {
	
	private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserClienteService userClienteService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserClienteService userClienteService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userClienteService = userClienteService;
    }
    
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
//        System.out.println("========= CHEGOU NO LOGIN =========");
//        System.out.println("Username recebido: " + request.username());
//        
//        try {
//            System.out.println("Tentando autenticar...");
//            Authentication auth = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                    request.username(),
//                    request.password()
//                )
//            );
//            
//            System.out.println("Autenticação bem-sucedida!");
//            
//            // Obtém o UserDetails do Spring Security
//            org.springframework.security.core.userdetails.User springUser = 
//                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
//            
//            // Extrai a role (supondo que a primeira role é a correta)
//            String role = springUser.getAuthorities().stream()
//                .findFirst()
//                .map(GrantedAuthority::getAuthority)
//                .orElse("ROLE_USER");
//            
//            // Cria um User temporário com os dados necessários
//            User user = new User();
//            user.setUsername(springUser.getUsername());
//            user.setRole(role);
//            
//            System.out.println("Role do usuário: " + user.getRole());
//            
//            String token = jwtService.gerarToken(user);
//            System.out.println("Token gerado: " + token);
//            
//            return ResponseEntity.ok(Map.of("token", token));
//
//        } catch (AuthenticationException e) {
//            System.out.println("ERRO NA AUTENTICAÇÃO: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(Map.of("error", "Credenciais inválidas"));
//        }
//    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        System.out.println("Tentativa de login para: " + request.username());
        
        try {
            // 1. Autenticação
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.username(),
                    request.password()
                )
            );
            
            // 2. Extrai UserDetails do objeto de autenticação
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 3. Obtém a role (garantindo o formato correto)
            String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new IllegalStateException("Usuário sem roles definidas"));
            
            System.out.println("Login bem-sucedido para: " + userDetails.getUsername() + " | Role: " + role);
            
            // 4. Gera o token incluindo a role
            String token = jwtService.generateToken(userDetails.getUsername(), role);
            
            // 5. Retorna a resposta padronizada
            return ResponseEntity.ok(token);
            
        } catch (AuthenticationException e) {
            System.out.println("Falha no login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciais inválidas"));
        }
    }
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
//        System.out.println("========= CHEGOU NO LOGIN =========");
//        System.out.println("Username recebido: " + request.username());
//        
//        try {
//            System.out.println("Tentando autenticar...");
//            Authentication auth = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                    request.username(),
//                    request.password()
//                )
//            );
//            
//            System.out.println("Autenticação bem-sucedida!");
//            User user = (User) auth.getPrincipal();
//            System.out.println("Role do usuário: " + user.getRole());
//            
//            String token = jwtService.gerarToken(user);
//            System.out.println("Token gerado: " + token);
//            
//            return ResponseEntity.ok(Map.of("token", token));
//
//        } catch (AuthenticationException e) {
//            System.out.println("ERRO NA AUTENTICAÇÃO: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body(Map.of("error", "Credenciais inválidas"));
//        }
//    }
//   

//    @PostMapping("/auth/login")
//    public ResponseEntity<Object> login(@RequestBody LoginRequestDTO loginRequest) {
//    	System.out.println("Tentando login para: " + loginRequest.username());
//    	
//        try {
//        	System.err.println("teste");
//        
//            User user = userClienteService.findByUsername(loginRequest.username());
//            
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado");
//            }
//         
//            if (!user.isUserAtivo()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário está inativo");
//            }
//        
//            Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                    loginRequest.username(),
//                    loginRequest.password()
//                )
//            );
//            
//            System.out.println("Autenticação bem-sucedida para: " + loginRequest.username());
//
//            String token = jwtService.gerarToken(user);
//
//            return ResponseEntity.ok(new LoginResponseDTO(token));
//        } catch (AuthenticationException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciais inválidas"));
//        }
//    }
//    
 

}
