package br.com.marcielli.bancom.service;

import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Profile("cliente")
public class JwtService {
	
	private final JwtDecoder jwtDecoder;
	
	public JwtService(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
	
	public String extractRole(String token) {
        // Se tiver "Bearer", ele remove
        token = token.replace("Bearer ", "");

        // Chama o decode para decodificar o token
        Jwt jwt = jwtDecoder.decode(token);

        // Extrai a role (pode ser "roles", "scope", "authorities", dependendo do token)
        String role = jwt.getClaim("roles"); // ou "scope", "authorities"

        return role;
    }

}
