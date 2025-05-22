package br.com.marcielli.bancom.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.marcielli.bancom.configuracao.AdminInitializer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.io.Decoders;

@Service
public class JwtService {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);  
	
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
        logger.error("Tamanho da chave:'{}'.", secretKeyString.getBytes(StandardCharsets.UTF_8).length * 8 + " bits");
        logger.error("========= CHAVE JWT CARREGADA =========");
    }
    
    public void debugToken(String token) {
        try {
        	logger.error("--- DEBUG TOKEN ---");
            String[] parts = token.split("\\.");
            logger.error("Header:'{}'.", new String(Base64.getUrlDecoder().decode(parts[0])));
            logger.error("Payload:'{}'.", new String(Base64.getUrlDecoder().decode(parts[1])));
            logger.error("Signature:'{}'.",parts[2]);
        } catch (Exception e) {
        	logger.error("Erro ao decodificar token:'{}'.",e.getMessage());
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
