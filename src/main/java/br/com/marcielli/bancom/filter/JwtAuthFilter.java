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
    	
    	// Pula verificação JWT para endpoints de login
    	if (request.getServletPath().equals("/auth/login") || 
                request.getServletPath().equals("/login")) {
                filterChain.doFilter(request, response); // Usando o parâmetro recebido
                return;
            }
    	
        String authHeader = request.getHeader("Authorization");
        System.out.println("Cabeçalho Authorization: " + authHeader);  
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	System.out.println("Cabeçalho Authorization ausente ou mal formatado.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.replace("Bearer ", "");
        System.out.println("Token extraído: " + token);
        try {
            Claims claims = jwtService.validateToken(token); // Usar JwtService
            System.out.println("Token validado com sucesso. Claims extraídas: " + claims);

            String username = claims.getSubject();
            String role = claims.get("role", String.class); // Role como string
            System.out.println("Username: " + username + ", Role: " + role); 
            

            if (username != null) {
            	System.out.println("Username encontrado: " + username);
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
                );
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                	    username, 
                	    null, 
                	    Collections.singletonList(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                	);
                System.out.println("Autenticação configurada no SecurityContext.");
                System.out.println("Authorities configuradas: " + authorities);
//                UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(username, null, authorities);
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//                System.out.println("Autenticação configurada no SecurityContext.");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
        System.out.println("Passando para o próximo filtro.");
    }

}
