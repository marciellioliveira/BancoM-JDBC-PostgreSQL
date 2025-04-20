package br.com.marcielli.BancoM.service;


import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.marcielli.BancoM.dto.AuthenticationRequestDTO;
import br.com.marcielli.BancoM.entity.AuthenticationResponse;
import br.com.marcielli.BancoM.entity.Token;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.repository.TokenRepository;
import br.com.marcielli.BancoM.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@Service
public class AuthenticationService {

	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	private final TokenRepository tokenRepository;

	private final AuthenticationManager authenticationManager;

	public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService,
			TokenRepository tokenRepository, AuthenticationManager authenticationManager) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.tokenRepository = tokenRepository;
		this.authenticationManager = authenticationManager;
	}

	public AuthenticationResponse register(User request) {

		// Se o usuário já existe, autentica ele.
		if (repository.findByUsername(request.getUsername()).isPresent()) {
			return new AuthenticationResponse(null, null, "O usuário já existe!");
		}

	
		User user = new User();
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setUsername(request.getUsername());
		user.setCliente(request.getCliente());
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		user.setRole(request.getRole());

		user = repository.save(user);

		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);

		System.err.println("UseR: "+user);
		System.err.println("Acces Tokenb "+accessToken);
		System.err.println("Refresh oken "+refreshToken);
		saveUserToken(accessToken, refreshToken, user);

		return new AuthenticationResponse(accessToken, refreshToken, "Cadastro realizado com sucesso.");

	}
	
	public AuthenticationResponse authenticate(AuthenticationRequestDTO request) {
		
		System.err.println(request.getUsername() +" - "+ request.getPassword());
	 authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

	 User user = repository.findByUsername(request.getUsername())
			    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
		    System.err.println(user);
		    // Adiciona os dados extras no token
		    Map<String, Object> extraClaims = new HashMap<>();
		    extraClaims.put("authorities", List.of(user.getRole().name())); // USER ou ADMIN
		    extraClaims.put("userId", user.getId());

		    System.err.println(user.getCliente());
		    if (user.getCliente() != null) {
		    	 System.err.println(user.getCliente());
		        extraClaims.put("clienteId", user.getCliente().getId());
		        System.err.println(extraClaims);
		    }

		    System.err.println("Antes de gerar o token de acesso");
		    // Usa o método do JwtService que aceita os extraClaims
		    String accessToken = jwtService.generateAccessToken(user, extraClaims);
		    String refreshToken = jwtService.generateRefreshToken(user);
		    
		    System.err.println(accessToken);
		    System.err.println(refreshToken);

		    revokeAllTokenByUser(user);
		    saveUserToken(accessToken, refreshToken, user);

		    return new AuthenticationResponse(accessToken, refreshToken, "Login realizado com sucesso.");

	}


//	public AuthenticationResponse authenticate(User request) {
//	 authenticationManager.authenticate(
//        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//    );
//
//	 User user = repository.findByUsername(request.getUsername())
//			    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
//		    System.err.println(user);
//		    // Adiciona os dados extras no token
//		    Map<String, Object> extraClaims = new HashMap<>();
//		    extraClaims.put("authorities", List.of(user.getRole().name())); // USER ou ADMIN
//		    extraClaims.put("userId", user.getId());
//
//		    System.out.println(user.getCliente());
//		    if (user.getCliente() != null) {
//		    	 System.out.println(user.getCliente());
//		        extraClaims.put("clienteId", user.getCliente().getId());
//		        System.out.println(extraClaims);
//		    }
//
//		    System.out.println("Antes de gerar o token de acesso");
//		    // Usa o método do JwtService que aceita os extraClaims
//		    String accessToken = jwtService.generateAccessToken(user, extraClaims);
//		    String refreshToken = jwtService.generateRefreshToken(user);
//		    
//		    System.err.println(accessToken);
//		    System.err.println(refreshToken);
//
//		    revokeAllTokenByUser(user);
//		    saveUserToken(accessToken, refreshToken, user);
//
//		    return new AuthenticationResponse(accessToken, refreshToken, "Login realizado com sucesso.");
//
//	}

	private void revokeAllTokenByUser(User user) {
		List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getId());
		if (validTokens.isEmpty()) {
			return;
		}

		validTokens.forEach(t -> {
			t.setLoggedOut(true);
		});

		tokenRepository.saveAll(validTokens);
	}

	private void saveUserToken(String accessToken, String refreshToken, User user) {
		Token token = new Token();
		token.setAccessToken(accessToken);
		token.setRefreshToken(refreshToken);
		token.setLoggedOut(false);
		token.setUser(user);
		tokenRepository.save(token);
	}

	public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {
		// Extrai o token da authorization header
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return new ResponseEntity(HttpStatus.UNAUTHORIZED);
		}

		String token = authHeader.substring(7);

		// Extrai o username do token
		String username = jwtService.extractUsername(token);

		// Vê se o usuário já existe no banco.
		User user = repository.findByUsername(username).orElseThrow(() -> new RuntimeException("O usuário não existe."));

		// Vê se o token é válido
		if (jwtService.isValidRefreshToken(token, user)) {
			// Gera o token de acesso.
			String accessToken = jwtService.generateAccessToken(user);
			String refreshToken = jwtService.generateRefreshToken(user);

			revokeAllTokenByUser(user);
			saveUserToken(accessToken, refreshToken, user);

			return new ResponseEntity(new AuthenticationResponse(accessToken, refreshToken, "Novo token gerado."),
					HttpStatus.OK);
		}

		return new ResponseEntity(HttpStatus.UNAUTHORIZED);

	}
}
