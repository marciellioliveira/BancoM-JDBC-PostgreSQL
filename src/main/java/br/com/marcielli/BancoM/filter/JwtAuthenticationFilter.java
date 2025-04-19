package br.com.marcielli.BancoM.filter;

import br.com.marcielli.BancoM.service.JwtService;
import br.com.marcielli.BancoM.service.UserDetailsServiceImp;
import io.jsonwebtoken.Claims;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsServiceImp userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImp userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, 
	                                 @NonNull HttpServletResponse response,
	                                 @NonNull FilterChain filterChain) throws ServletException, IOException {

		// Recupera o token JWT da requisição
	    String authHeader = request.getHeader("Authorization");

	    // Se não há token no header, libera a requisição para os outros filtros
	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        filterChain.doFilter(request, response);
	        return;
	    }

	    String token = authHeader.substring(7);

	    // Extrai as claims do token diretamente
	    Claims claims = jwtService.extractAllClaims(token);
	    String username = claims.getSubject();
	    Long userId = claims.get("userId", Long.class);
	    Long clienteId = claims.get("clienteId", Long.class);

	    request.setAttribute("userId", userId);
	    request.setAttribute("clienteId", clienteId);

	    // Verifica se o contexto ainda não está autenticado
	    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

	        // Extrai a lista de roles (authorities) da claim
	        List<String> roles = claims.get("authorities", List.class);
	        
	        if (roles == null) {
	            roles = new ArrayList<>(); // Se for null, inicializa uma lista vazia
	        }

	        // Converte para lista de GrantedAuthority
	        List<GrantedAuthority> authorities = roles.stream()
	                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Adicionando o prefixo "ROLE_"
	                .collect(Collectors.toList());

	        // Cria token de autenticação usando o que veio do JWT
	        UsernamePasswordAuthenticationToken authToken =
	                new UsernamePasswordAuthenticationToken(username, null, authorities);

	        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

	        // Define o contexto de segurança com o token gerado
	        SecurityContextHolder.getContext().setAuthentication(authToken);
	    }
	    
	    String role = claims.get("role", String.class);
	    if (role != null && role.equals("ADMIN")) {
	        // Logado como Funcionário
	        response.addHeader("Logged-As", "Logado como Funcionário: " + username);
	    } else {
	        // Caso o role seja null ou não seja ADMIN
	        response.addHeader("Logged-As", "Logado como Cliente: " + username);
	    }

	    // Aqui você pode adicionar lógica para verificar permissões específicas para Admin ou Cliente
	    if (isAdminRequest(request) && !isAdmin(request)) {
	        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
	        return;
	    }

	    if (isClienteRequest(request) && !isCliente(request, clienteId)) {
	        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
	        return;
	    }

	    // Continua o fluxo da requisição
	    filterChain.doFilter(request, response);
	}
	
	// Verifica se a rota requer um usuário Admin
	private boolean isAdminRequest(HttpServletRequest request) {
	    return request.getRequestURI().startsWith("/admin");
	}

	// Verifica se o usuário logado é um Admin
	private boolean isAdmin(HttpServletRequest request) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    return auth.getAuthorities().stream()
	            .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
	}

	// Verifica se a rota requer um cliente específico
	private boolean isClienteRequest(HttpServletRequest request) {
	    return request.getRequestURI().startsWith("/clientes");
	}

	// Verifica se o usuário logado é o cliente correto
	private boolean isCliente(HttpServletRequest request, Long clienteId) {
	    Long clienteIdToken = (Long) request.getAttribute("clienteId");
	    return clienteIdToken.equals(clienteId);
	}
	
	

}
