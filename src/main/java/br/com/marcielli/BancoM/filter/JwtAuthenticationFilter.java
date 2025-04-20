package br.com.marcielli.BancoM.filter;

import br.com.marcielli.BancoM.service.JwtService;
import br.com.marcielli.BancoM.service.UserDetailsServiceImp;
import io.jsonwebtoken.Claims;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
	private final UserDetailsServiceImp userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImp userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}
	
	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, 
	                                 @NonNull HttpServletResponse response,
	                                 @NonNull FilterChain filterChain) throws ServletException, IOException {

	    String authHeader = request.getHeader("Authorization");

	    // Se o header estiver ausente ou mal formatado, segue o fluxo normalmente
	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        filterChain.doFilter(request, response);
	        return;
	    }

	    String token = authHeader.substring(7); // Remove o prefixo "Bearer "
	    Claims claims;

	    try {
	        // Tenta extrair as claims do token
	        claims = jwtService.extractAllClaims(token);
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

	    // Autentica o usuário se ainda não estiver autenticado
	    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
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

	    // Verificações de permissão para Admin e Cliente
	    if (isAdminRequest(request) && !isAdmin(request)) {
	        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
	        return;
	    }

	    if (isClienteRequest(request) && !isCliente(request, clienteId)) {
	        logger.warn("Acesso negado: clienteId inválido ou rota não permitida.");
	        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
	        return;
	    }

	    // Continua o fluxo da requisição
	    filterChain.doFilter(request, response);
	}

	
//	@Override
//	protected void doFilterInternal(@NonNull HttpServletRequest request, 
//	                                 @NonNull HttpServletResponse response,
//	                                 @NonNull FilterChain filterChain) throws ServletException, IOException {
//		
//		String authHeader = request.getHeader("Authorization");
//		
//	    // Se não há token no header, libera a requisição para os outros filtros
//	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//	    	//logger.info("Token JWT não encontrado ou mal formatado.");
//	        filterChain.doFilter(request, response);
//	        return;
//	    }
//
//	    String token = authHeader.substring(7); //Pegando somente o token sem o Bearer
//	    Claims claims;
//	    
//	    try {
//	        claims = jwtService.extractAllClaims(token);
//	        
//	     // Pegar os dados do token
//	        Object clienteId = claims.get("clienteId");
//	        Object authorities = claims.get("authorities");
//	        
//	     // Salvar no request
//	        request.setAttribute("clienteId", clienteId);
//	        request.setAttribute("authorities", authorities);
//	        
//	    } catch (Exception e) {
//	        logger.error("Erro ao extrair claims do token: " + e.getMessage());
//	        filterChain.doFilter(request, response);
//	        return;
//	    }
//	    String username = claims.getSubject();
//	    Long userId = claims.get("userId", Long.class);
//	    Long clienteId = claims.get("clienteId", Long.class);
//	    System.out.println("clienteId extraído do JWT: " + clienteId);
//
//	    request.setAttribute("userId", userId);
//	    request.setAttribute("clienteId", clienteId);
//	    System.out.println("clienteId adicionado ao request: " + clienteId);
//
//	    // Verifica se o contexto ainda não está autenticado
//	    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//
//	        // Extrai a lista de roles (authorities) da claim
//	        List<String> roles = claims.get("authorities", List.class);
//	        
//	        if (roles == null) {
//	            roles = new ArrayList<>(); // Se for null, inicializa uma lista vazia
//	        }
//
//	        // Converte para lista de GrantedAuthority
//	        List<GrantedAuthority> authorities = roles.stream()
//	                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Adicionando o prefixo "ROLE_"
//	                .collect(Collectors.toList());
//
//	        // Cria token de autenticação usando o que veio do JWT
//	        UsernamePasswordAuthenticationToken authToken =
//	                new UsernamePasswordAuthenticationToken(username, null, authorities);
//
//	        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//	        // Define o contexto de segurança com o token gerado
//	        SecurityContextHolder.getContext().setAuthentication(authToken);
//	    }
//	   
//	    String role = claims.get("role", String.class);
//	    if (role != null && role.equals("ADMIN")) {
//	        // Logado como Funcionário
//	        response.addHeader("Logged-As", "Logado como Funcionário: " + username);
//	    } else {
//	        // Caso o role seja null ou não seja ADMIN
//	        response.addHeader("Logged-As", "Logado como Cliente: " + username);
//	    }
//
//	    // Aqui você pode adicionar lógica para verificar permissões específicas para Admin ou Cliente
//	    if (isAdminRequest(request) && !isAdmin(request)) {
//	        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
//	        return;
//	    }
//
//	    if (isClienteRequest(request) && !isCliente(request, clienteId)) {
//	    	 System.err.println("Negado! clienteId inválido ou rota não permitida.");
//	    	 response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
//	        //response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar essa rota.");
//	        return;
//	    }
//
//	    // Continua o fluxo da requisição
//	    filterChain.doFilter(request, response);
//	}
	
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
		System.out.println("HttpServletRequest (isClienteRequest(HttpServletRequest request)) = " + request);
	    return request.getRequestURI().startsWith("/clientes");
	}

	// Verifica se o usuário logado é o cliente correto
	private boolean isCliente(HttpServletRequest request, Long clienteId) {
		System.err.println();
		 System.out.println("clienteId da URL (isCliente(HttpServletRequest request, Long clienteId)) = " + clienteId);
		 System.out.println("HttpServletRequest (isCliente(HttpServletRequest request, Long clienteId)) = " + request);
	    Long clienteIdToken = (Long) request.getAttribute("clienteId");
	    
	    if (clienteIdToken == null) {
	        System.out.println("clienteIdToken é null!");
	        // Se o clienteId não for encontrado, talvez seja um admin, você pode fazer a verificação conforme necessário
	        // Exemplo: Verificar se a rota exige um cliente específico ou se é uma rota de admin
	        // Negue o acesso ou passe normalmente, dependendo do caso
	        System.out.println("Negado! clienteId inválido ou rota não permitida.");
	        // Você pode lançar uma exceção ou fazer outro tipo de controle
	    } else {
	        // Se clienteIdToken não for null, então é um usuário com clienteId válido
	        System.out.println("clienteIdToken extraído com sucesso: " + clienteIdToken);
	    }
	    
	    if (clienteIdToken == null) {
	        System.err.println("clienteIdToken é null!");
	        // Pega roles para checar se é admin
	        List<String> roles = (List<String>) request.getAttribute("authorities");
	        
	        if (roles != null && roles.contains("ADMIN")) {
	            System.out.println("Acesso liberado para ADMIN");
	            return true;
	        }
	        return false; // Ou qualquer outra lógica para lidar com isso
	    }
	    System.out.println("clienteIdToken: " + clienteIdToken);
	   
	    return clienteIdToken.equals(clienteId);
	}
	
	

}
