package br.com.marcielli.bancom.controller;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.bancom.dto.LoginRequestDTO;
import br.com.marcielli.bancom.dto.LoginResponseDTO;
import br.com.marcielli.bancom.repository.UserRepository;
import br.com.marcielli.bancom.service.RedisTokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class TokenController { // Passo 2

	private final JwtEncoder jwtEncoder;
	private final UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;
	private final RedisTokenBlacklistService tokenBlacklistService;

	public TokenController(JwtEncoder jwtEncoder, UserRepository userRepository,
			BCryptPasswordEncoder bCryptPasswordEncoder, RedisTokenBlacklistService tokenBlacklistService) {
		this.jwtEncoder = jwtEncoder;
		this.userRepository = userRepository;
		this.passwordEncoder = bCryptPasswordEncoder;
		this.tokenBlacklistService = tokenBlacklistService;
	}

	// Criei dois DTOS para Login
	// 1. LoginRequestDTO que é um record e passa (String username, String password)
	// como parametro.
	// 2. LoginResponseDTO que é um record e passa (String accessToken, Long
	// expiresIn) como parametro.

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {

		var user = userRepository.findByUsername(loginRequest.username())
				.orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

		if (!user.isLoginCorrect(loginRequest, passwordEncoder)) {
			throw new BadCredentialsException("Credenciais inválidas");
		}

		var now = Instant.now();
		var expiresIn = 3600L;

		var authorities = user.getRoles().stream().map(role -> "SCOPE_" + role.getName().toUpperCase())
				.collect(Collectors.joining(" "));

		var claims = JwtClaimsSet.builder().issuer("mybackend").subject(user.getId().toString()).issuedAt(now)
				.expiresAt(now.plusSeconds(expiresIn)).claim("scope", authorities).claim("roles", authorities).build();

		var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		return ResponseEntity.ok(new LoginResponseDTO(jwtValue, expiresIn));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		String token = extractToken(request);
		if (token != null) {
			tokenBlacklistService.invalidateToken(token);
		}
		return ResponseEntity.ok().build();
	}

	private String extractToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	@GetMapping("/login")
	public ResponseEntity<String> loginPage(@RequestParam(required = false) String logout) {
		if (logout != null) {
			return ResponseEntity.ok("Logout realizado com sucesso!");
		}
		return ResponseEntity.ok("Página de login");
	}
}
