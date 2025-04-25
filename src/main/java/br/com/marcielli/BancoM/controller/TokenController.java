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
	    var user = userRepository.findByUsername(loginRequest.username())
	        .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

	    if (!user.isLoginCorrect(loginRequest, passwordEncoder)) {
	        throw new BadCredentialsException("Credenciais inválidas");
	    }

	    var now = Instant.now();
	    var expiresIn = 3600L;

	    var authorities = user.getRoles().stream()
	            .map(role -> "SCOPE_" + role.getName().toUpperCase())
	            .collect(Collectors.joining(" "));

	    var claims = JwtClaimsSet.builder()
	            .issuer("mybackend")
	            .subject(user.getId().toString())
	            .issuedAt(now)
	            .expiresAt(now.plusSeconds(expiresIn))
	            .claim("scope", authorities)
	            .claim("roles", authorities) 
	            .build();

	    var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	    
	    return ResponseEntity.ok(new LoginResponseDTO(jwtValue, expiresIn));
	}
}
