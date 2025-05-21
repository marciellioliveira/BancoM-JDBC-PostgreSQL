package br.com.marcielli.bancom.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.io.Decoders;

@Service
public class JwtService {
	
	@Value("${jwt.secret}")
    private String secretKeyString;
	
	// Método para criar a chave HMAC
	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        return Keys.hmacShaKeyFor(keyBytes);
	}
	
	// Configuração do parser
	public JwtParser getJwtParser() {
	    return Jwts.parser()
	        .verifyWith(getSigningKey()) // Agora recebendo SecretKey corretamente
	        .build();
	}
    
    @PostConstruct
    public void init() {
        System.out.println("Secret key sendo usada: " + secretKeyString);
        System.out.println("Tamanho da chave: " + secretKeyString.getBytes(StandardCharsets.UTF_8).length * 8 + " bits");
        
        
        System.out.println("========= CHAVE JWT CARREGADA =========");
        System.out.println("Chave: " + secretKeyString);
        System.out.println("Tamanho: " + Decoders.BASE64.decode(secretKeyString).length * 8 + " bits");
        System.out.println("Hash da chave: " + Arrays.hashCode(Decoders.BASE64.decode(secretKeyString)));
    }
    
    public void debugToken(String token) {
        try {
            System.out.println("--- DEBUG TOKEN ---");
            String[] parts = token.split("\\.");
            System.out.println("Header: " + new String(Base64.getUrlDecoder().decode(parts[0])));
            System.out.println("Payload: " + new String(Base64.getUrlDecoder().decode(parts[1])));
            System.out.println("Signature: " + parts[2]);
        } catch (Exception e) {
            System.out.println("Erro ao decodificar token: " + e.getMessage());
        }
    }
 
  
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public String generateToken(String username, String role) {
        // Remove "ROLE_" se já existir
        String cleanRole = role.startsWith("ROLE_") ? role.substring(5) : role;
        
        return Jwts.builder()
                .subject(username)
                .claim("role", cleanRole) // Armazena como "BASIC" ou "ADMIN"
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(getSigningKey())
                .compact();
    }
    
//    private String extractRole(UserDetails userDetails) {
//        return userDetails.getAuthorities().stream()
//            .findFirst()
//            .map(GrantedAuthority::getAuthority)
//            .map(role -> role.replace("ROLE_", ""))
//            .orElse("USER");
//    }


    public <T> T extrairClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = validateToken(token);
        return claimsResolver.apply(claims);
    }

    public String extrairUsername(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public boolean tokenExpirado(String token) {
        return extrairClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean tokenValido(String token, String username) {
        try {
            final String usernameEncontrado = extrairUsername(token);
            return usernameEncontrado.equals(username) && !tokenExpirado(token);
        } catch (RuntimeException ex) {
            return false;
        }
    }
	
	
}
