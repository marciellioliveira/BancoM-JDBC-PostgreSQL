package br.com.marcielli.BancoM.controller;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import br.com.marcielli.BancoM.dto.LoginRequestDTO;
import br.com.marcielli.BancoM.dto.LoginResponseDTO;
import br.com.marcielli.BancoM.entity.Role;
import br.com.marcielli.BancoM.repository.UserRepository;

@RestController
public class TokenController { // Passo 2

	private final JwtEncoder jwtEncoder;
	private final UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;

	public TokenController(JwtEncoder jwtEncoder, UserRepository userRepository,
			BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.jwtEncoder = jwtEncoder;
		this.userRepository = userRepository;
		this.passwordEncoder = bCryptPasswordEncoder;
	}

	// Criei dois DTOS para Login
	// 1. LoginRequestDTO que é um record e passa (String username, String password)
	// como parametro.
	// 2. LoginResponseDTO que é um record e passa (String accessToken, Long
	// expiresIn) como parametro.

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
		// Ir no banco de dados ver se o usário existe e depois comparar as senhas e por
		// isso vamos injetar o UserRepository

		var user = userRepository.findByUsername(loginRequest.username());

		if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)) { // Se estiver vazio - Criar o
																							// método
																							// (isLoginCorrect(loginRequest,
																							// bCryptPasswordEncoder))
																							// na classe User
			throw new BadCredentialsException("Usuário ou senha está inválido.");

			// Como estou negando (!user.get().isLoginCorrect(loginRequest,
			// bCryptPasswordEncoder)), se o login não estiver correto ele cai ali também no
			// throw
		}

		// Para comparar se a senha que o usuário passou é igual a que existe no banco.
		// Para isso precisamos fazer mecanismo de comparação de senhas com uma tecnica
		// avançada de criptografia
		// Então vamos criar mais um Bean na classe do Spring Security (SecurityConfig)

		// Se o login estiver correto, ele cai aqui
		// Agora preciso gerar o token JWT e retornar na requisição
		// Para gerar o JWT, preciso configurar os atributos do JSON que são chamados de
		// claims

		var now = Instant.now();
		var expiresIn = 300L;

		var scopes = user.get().getRoles().stream().map(Role::getName).collect(Collectors.joining(" "));

		// No builder vamos definir os campos
		var claims = JwtClaimsSet.builder().issuer("mybackend").subject(user.get().getId().toString()).issuedAt(now) // data
																														// da
																														// emissão
																														// do
																														// token
				.expiresAt(now.plusSeconds(expiresIn)) // expiração vai ser agora + 300S = daqui 5 minutos
				.claim("scope", scopes).build();

		var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(); // Pega o valor do token
																								// JWT com o claim
																								// através do Encoder

		return ResponseEntity.ok(new LoginResponseDTO(jwtValue, expiresIn));

	}
}
