package br.com.marcielli.BancoM.filter;

import br.com.marcielli.BancoM.service.JwtService;
import br.com.marcielli.BancoM.service.UserDetailsServiceImp;
import io.jsonwebtoken.Claims;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        filterChain.doFilter(request, response);
	        return;
	    }

	    String token = authHeader.substring(7);

	    // Extrai as claims do token diretamente
	    Claims claims = jwtService.extractAllClaims(token);
	    String username = claims.getSubject();

	    // Verifica se o contexto ainda não está autenticado
	    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

	        //  Extrai a lista de roles (authorities) da claim, para nunca ser = null
	        List<String> roles = claims.get("authorities", List.class);
	        
	        if (roles == null) {
	            roles = new ArrayList<>(); // Se for null, inicializa uma lista vazia
	        }

	        //  Converte para lista de GrantedAuthority
	        List<GrantedAuthority> authorities = roles.stream()
	                .map(SimpleGrantedAuthority::new)
	                .collect(Collectors.toList());

	        //  Cria token de autenticação usando apenas o que veio do JWT
	        UsernamePasswordAuthenticationToken authToken =
	                new UsernamePasswordAuthenticationToken(username, null, authorities);

	        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

	        SecurityContextHolder.getContext().setAuthentication(authToken);
	    }

	    filterChain.doFilter(request, response);
	}

//	@Override
//	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
//			@NonNull FilterChain filterChain) throws ServletException, IOException {
//
//		String authHeader = request.getHeader("Authorization");
//
//		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//
//		String token = authHeader.substring(7);
//		String username = jwtService.extractUsername(token);
//
//		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//
//			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//			if (jwtService.isValid(token, userDetails)) {
//				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
//						null, userDetails.getAuthorities());
//
//				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//				SecurityContextHolder.getContext().setAuthentication(authToken);
//			}
//		}
//		filterChain.doFilter(request, response);
//
//	}

}
