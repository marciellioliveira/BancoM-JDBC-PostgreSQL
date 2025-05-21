package br.com.marcielli.bancom.filter;

import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import br.com.marcielli.bancom.service.JwtService;
import br.com.marcielli.bancom.service.UserClienteService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.InsufficientAuthenticationException;


@Component			
public class JwtAuthFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
    private final UserClienteService usuarioService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    public JwtAuthFilter(JwtService jwtService, UserClienteService usuarioService, AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
        throws IOException, ServletException {
        
    	logger.info("Processando requisição para: {}", request.getRequestURI());
    	
        // Pula filtro para endpoints de login
        if (request.getServletPath().equals("/login") || request.getServletPath().equals("/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai o token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.validateToken(token);
            
            // Recupera informações do token
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            logger.info("Role no JWT Auth Filter: {}", role);
            // Cria objeto de autenticação
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );

            // Configura no contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (AuthenticationException ex) {
            throw ex; // delega para o AuthenticationEntryPoint automaticamente
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
