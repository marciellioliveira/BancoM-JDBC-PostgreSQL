package br.com.marcielli.bancom.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.Algorithm;

import br.com.marcielli.bancom.entity.User;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;

import java.security.Key;

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
    
//    public Claims validateToken(String token) {
//        try {
//            System.out.println("Validando token com chave: " + secretKeyString);
//            return Jwts.parser()
//                    .verifyWith(getSigningKey())
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//        } catch (ExpiredJwtException ex) {
//            System.out.println("Token expirado: " + ex.getMessage());
//            throw new RuntimeException("Token expirado", ex);
//        } catch (SignatureException ex) {
//
//        	//Inicio
//        	
//        	System.out.println("ERRO DE ASSINATURA - DEBUG DETALHADO:");
//            System.out.println("1. Chave secreta atual: " + secretKeyString);
//            System.out.println("2. Token recebido: " + token);
//            
//            // Verificar o header do token
//            try {
//                String[] parts = token.split("\\.");
//                if (parts.length > 0) {
//                    String header = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
//                    System.out.println("3. Header do token: " + header);
//                }
//            } catch (Exception e) {
//                System.out.println("3. Não foi possível decodificar o header do token");
//            }
//            
//            // Verificar algoritmo usado - forma correta para jjwt 0.12.x
//            try {
//                Jwt<?, ?> jwt = Jwts.parser()
//                                 .unsecured()
//                                 .build()
//                                 .parse(token);
//                
//                System.out.println("4. Algoritmo no header: " + jwt.getHeader().getAlgorithm());
//                System.out.println("5. Tipo do token: " + jwt.getHeader().getType());
//            } catch (Exception e) {
//                System.out.println("4. Falha ao detectar algoritmo: " + e.getMessage());
//            }
//            
//            throw new RuntimeException("Assinatura do token inválida", ex);
//        	
//        	//Fim
//
//        } catch (JwtException | IllegalArgumentException ex) {
//            System.out.println("Erro ao validar token: " + ex.getMessage());
//            throw new RuntimeException("Erro ao validar o token", ex);
//        }
//    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas
                .signWith(getSigningKey())
                .compact();
    }
    
    private String extractRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .map(role -> role.replace("ROLE_", ""))
            .orElse("USER");
    }
    
    
    
//    public String gerarToken(User usuario) {
//        // Garanta que está incluindo a role como claim
//        return Jwts.builder()
//            .claim("role", usuario.getRole())  // Linha crucial!
//            .subject(usuario.getUsername())
//            .issuedAt(new Date(System.currentTimeMillis()))
//            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 dia
//            .signWith(getSigningKey())
//            .compact();
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
	
	

//	@Value("${jwt.secret}")
//    private String secretKeyString;
//
//	// Obtém a chave secreta para assinatura
//    private SecretKey getSigningKey() {
//        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
//    }
//
//    //Validando o token e retornando as claims
//    public Claims validateToken(String token) {      	
//    	 System.out.println("Validando token: " + token); 
//    	 Claims claims = Jwts.parser()
//    	            .verifyWith(getSigningKey())
//    	            .build()
//    	            .parseSignedClaims(token)
//    	            .getPayload();
//        System.out.println("Claims extraídas: " + claims);
//        return claims;
//    }
//
//    public String gerarToken(User usuario) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("role", usuario.getRole());
//        System.out.println("Gerando token para o usuário: " + usuario.getUsername());
//
//        String token = Jwts.builder()
//                .claims(claims)
//                .subject(usuario.getUsername())
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Expira em 1 dia
//                .signWith(getSigningKey())
//                .compact();
//        System.out.println("Token gerado: " + token);
//        return token;
//    }
//
//    public <T> T extrairClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extrairTodosOsClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    public String extrairUsername(String token) {
//        return extrairClaim(token, Claims::getSubject);
//    }
//
//    private Claims extrairTodosOsClaims(String token) {
//    	System.out.println("Extraindo todos os claims do token: " + token);  // Exibe o token
//        return Jwts.parser()
//            .verifyWith(getSigningKey())
//            .build()
//            .parseSignedClaims(token)
//            .getPayload();
//    }
//
//    private Date extrairExpiration(String token) {
//        return extrairClaim(token, Claims::getExpiration);
//    }
//
//    public boolean tokenExpirado(String token) {
//    	 boolean expirado = extrairExpiration(token).before(new Date());
//         System.out.println("O token expirou? " + expirado);  
//         return expirado;
//    }
//
//    public boolean tokenValido(String token, String username) {
//        final String usernameEncontrado = extrairUsername(token);
//        boolean valido = (usernameEncontrado.equals(username) && !tokenExpirado(token));
//        System.out.println("Token válido? " + valido); 
//        return valido;
//    }
}
