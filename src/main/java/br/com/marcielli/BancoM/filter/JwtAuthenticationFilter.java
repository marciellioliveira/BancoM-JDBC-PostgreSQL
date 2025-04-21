package br.com.marcielli.BancoM.filter;

import br.com.marcielli.BancoM.entity.CustomUserDetails;
import br.com.marcielli.BancoM.service.JwtService;
import br.com.marcielli.BancoM.service.UserDetailsServiceImp;
import io.jsonwebtoken.Claims;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
		
	@Lazy
	private final UserDetailsServiceImp userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImp userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, 
	                                 @NonNull HttpServletResponse response,
	                                 @NonNull FilterChain filterChain) throws ServletException, IOException {
		
		String path = request.getServletPath();
		if (path.equals("/register") || path.equals("/auth/login")) {
		    filterChain.doFilter(request, response);
		    return;
		}

	    String authHeader = request.getHeader("Authorization");

	    // Se o header estiver ausente ou mal formatado, segue o fluxo normalmente
	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        filterChain.doFilter(request, response);
	        return;
	    }

	    String token = authHeader.substring(7); // Remove o prefixo "Bearer "
	    Claims claims;
//	    Long clienteId = null;

	    try {
	        // Tenta extrair as claims do token
	        claims = jwtService.extractAllClaims(token);
//	        clienteId = claims.get("clienteId", Long.class);
//	        System.out.println("clienteId extraído do token: " + clienteId);
	        
	    } catch (Exception e) {
	        logger.error("Erro ao extrair claims do token: " + e.getMessage());
	        filterChain.doFilter(request, response);
	        return;
	    }

	    // Extrai informações do token
	    String username = claims.getSubject();
	    Long userId = claims.get("userId", Long.class);
	    Long clienteId = claims.get("clienteId", Long.class);
	    String role = claims.get("role", String.class);
	    List<String> roles = claims.get("authorities", List.class);

	    // Garante que a lista de roles não seja nula
	    if (roles == null) roles = new ArrayList<>();

	    // Salva atributos no request para reutilização em outras partes da aplicação
	    request.setAttribute("userId", userId);
	    request.setAttribute("clienteId", clienteId);
	    request.setAttribute("authorities", roles);
	    
	    CustomUserDetails userDetails = new CustomUserDetails(username, null, clienteId, roles.stream()
	            .map(role1 -> new SimpleGrantedAuthority("ROLE_" + role))
	            .collect(Collectors.toList())); 
	    userDetails.setUsername(username);
	    userDetails.setClienteId(clienteId);
	    userDetails.setAuthorities(roles.stream().map(SimpleGrantedAuthority::new).toList());

	    // Autentica o usuário se ainda não estiver autenticado
	    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	    	
	    	 // Carregue os detalhes do usuário (CustomUserDetails) a partir do username
	    	Object userDetailsObj = userDetailsService.loadUserByUsername(username);
	    	
	    	if (userDetailsObj instanceof CustomUserDetails) {
	    	    CustomUserDetails userDetails1 = (CustomUserDetails) userDetailsObj;
	    	    // Agora você pode usar 'userDetails1' como CustomUserDetails
	    	} else {
	    	    // Tratar o caso onde o tipo não é CustomUserDetails
	    	    logger.error("O tipo retornado não é CustomUserDetails.");
	    	}
	        
	        List<GrantedAuthority> authorities = roles.stream()
	                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
	                .collect(Collectors.toList());

	        UsernamePasswordAuthenticationToken authToken =
	                new UsernamePasswordAuthenticationToken(username, null, authorities);

	        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	        SecurityContextHolder.getContext().setAuthentication(authToken);
	    }

	    // Adiciona um header para indicar o tipo de usuário logado
	    if ("ADMIN".equals(role)) {
	        response.addHeader("Logged-As", "Logado como Funcionário: " + username);
	    } else {
	        response.addHeader("Logged-As", "Logado como Cliente: " + username);
	    }

	    
//	    // Verificações de permissão para Admin e Cliente
//	    if (isAdminRequest(request) && !isAdmin(request)) {
//	    	
//	        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
//	        return;
//	    }
//
//	    if (isClienteRequest(request) && !isCliente(request, clienteId)) {
//	        logger.warn("Acesso negado: clienteId inválido ou rota não permitida.");
//	        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
//	        return;
//	    }

	    System.err.println("request: "+request);
	    System.err.println("response: "+response);
	    // Continua o fluxo da requisição
	    filterChain.doFilter(request, response);
	}

	
	// Verifica se a rota requer um usuário Admin
	private boolean isAdminRequest(HttpServletRequest request) { //Verifica se a URL acessada exige permissão de admin.
		System.err.println("isAdminRequest: "+request);
	    return request.getRequestURI().startsWith("/admin");
	}

	// Verifica se o usuário logado é um Admin
	private boolean isAdmin(HttpServletRequest request) { //Verifica se o usuário logado tem a role ADMIN.
		System.err.println("isAdmin: "+request);
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    return auth.getAuthorities().stream()
	            .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
	}

	// Verifica se a rota requer um cliente específico
	private boolean isClienteRequest(HttpServletRequest request) { //Verifica se a rota acessada é uma rota de cliente.
		System.err.println("isClienteRequest: "+request);
	    return request.getRequestURI().startsWith("/clientes");
	}

	private boolean isCliente(HttpServletRequest request, Long clienteId) { //Verifica se o usuário logado tem permissão para acessar os dados do cliente específico
		System.err.println("isCliente: "+request);
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    Object principal = auth.getPrincipal();

	    if (principal instanceof CustomUserDetails) {
	        CustomUserDetails userDetails = (CustomUserDetails) principal;
	        Long clienteIdToken = userDetails.getClienteId();

	        System.out.println("clienteId da URL: " + clienteId);
	        System.out.println("clienteIdToken do token: " + clienteIdToken);

	        // Se clienteIdToken for null, talvez seja um admin. Verificar as roles.
	        if (clienteIdToken == null) {
	            System.out.println("clienteIdToken é null, verificando se é ADMIN...");

	            List<String> roles = (List<String>) request.getAttribute("authorities");
	            if (roles != null && roles.contains("ADMIN")) {
	                System.out.println("Acesso liberado para ADMIN");
	                return true;
	            } else {
	                System.out.println("Acesso negado: clienteIdToken nulo e não é ADMIN");
	                return false;
	            }
	        }

	        // Se clienteIdToken não for null, compara com clienteId
	        return clienteIdToken.equals(clienteId);
	    } else {
//	        System.err.println("Principal não é CustomUserDetails, é: " + principal.getClass().getName());
	        return false;
	    }
	}
}
	

