package br.com.marcielli.bancom.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.marcielli.bancom.service.JwtService;
import br.com.marcielli.bancom.service.UserClienteService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component			
public class JwtAuthFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
    private final UserClienteService usuarioService;

    public JwtAuthFilter(JwtService jwtService, UserClienteService usuarioService) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Pula endpoints públicos (opcional, apenas para performance)
        if (request.getServletPath().equals("/auth/login") || request.getServletPath().equals("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verifica se há token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Se não houver token, DEIXA O SPRING SECURITY DECIDIR (não bloqueia aqui)
            filterChain.doFilter(request, response);
            return;
        }

        // Processa token quando existir
        try {
            String token = authHeader.substring(7).trim();
            
         // Verifica se o token está encapsulado em JSON
            if (token.startsWith("{") && token.contains("\"token\":")) {
                try {
                    JsonNode jsonNode = new ObjectMapper().readTree(token);
                    token = jsonNode.get("token").asText();
                } catch (IOException e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Formato de token inválido");
                    return;
                }
            }
            
            
            
            
            
            Claims claims = jwtService.validateToken(token);
            String username = claims.getSubject();
            String role = "ROLE_" + claims.get("role", String.class);

            // Debug importante
            System.out.println("Token recebido: " + token); // Deve mostrar APENAS o JWT

            // Configura autenticação
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            System.err.println("[JWT FILTER] Erro no token: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        filterChain.doFilter(request, response);
    }
    
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//    	
//    	// Pula endpoints públicos
//    	if (request.getServletPath().equals("/auth/login") || request.getServletPath().equals("/login")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//    	
//    	// Valida header
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token ausente");
//            return;
//        }
//
//        // Extrai e valida token
//        String token = authHeader.substring(7); // Remove "Bearer "
//        try {
//        	Claims claims = jwtService.validateToken(token);
//            String username = claims.getSubject();
//            String role = "ROLE_" + claims.get("role", String.class); // Garante prefixo
//            
//         // Configura autenticação no contexto
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                username,
//                null,
//                Collections.singletonList(new SimpleGrantedAuthority(role))
//            );
//            
//            SecurityContextHolder.getContext().setAuthentication(authToken);
//            
//        } catch (Exception e) {
//        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
//            return;
//        }
//
//        filterChain.doFilter(request, response);
//    }

}
