package br.com.marcielli.bancom.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.marcielli.bancom.service.JwtService;
import br.com.marcielli.bancom.service.UserClienteService;
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

	@Override     //esse método vai ser chamado toda vez que uma requisição for feita
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
			FilterChain filterChain)
			throws ServletException, IOException {
		// aqui eu vou pegar o token do header da requisição
		final String authHeader = request.getHeader("Authorization"); // pega o cabeçalho de autorização
		final String jwt;
		final String username;
		//o cabeçalho é responsável por mandar informações pro servidor/memoria
		//verifico se o cabeçalho não é nulo e começa com Bearer
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response); // deixa passar a requisição pro próximo filtro ou pro controller
			return; // se não tiver o token, não precisa continuar
		}
		
		jwt = authHeader.substring(7); // removo o Bearer e um espaço (7 letras)
		username = jwtService.extrairUsername(jwt); // pego o email do token
		
		//aqui é o processo pra validar o token
		if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = usuarioService.loadUserByUsername(username); // carrega o usuário do banco de dados
			
			if (jwtService.tokenValido(jwt, userDetails.getUsername())) { // verifica se o token é válido
				// aqui eu crio a autenticação do usuário
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities()); // cria o token de autenticação
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); 
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		filterChain.doFilter(request, response); // deixa passar a requisição pro próximo filtro ou pro controller
	}

}
