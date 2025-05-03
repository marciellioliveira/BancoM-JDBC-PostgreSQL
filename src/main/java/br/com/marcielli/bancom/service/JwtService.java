package br.com.marcielli.bancom.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.marcielli.bancom.entity.User;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;




@Service
public class JwtService {
	
	@Value("${jwt.secret}")
    private String secretKeyString;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }
    
    @PostConstruct
    public void init() {
        System.out.println("Secret key sendo usada: " + secretKeyString);
        System.out.println("Tamanho da chave: " + secretKeyString.getBytes(StandardCharsets.UTF_8).length * 8 + " bits");
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
        try {
            System.out.println("Validando token com chave: " + secretKeyString);
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            System.out.println("Token expirado: " + ex.getMessage());
            throw new RuntimeException("Token expirado", ex);
        } catch (SignatureException ex) {
            System.out.println("ERRO DE ASSINATURA - Possíveis causas:");
            System.out.println("1. Chave secreta incorreta");
            System.out.println("2. Token modificado");
            System.out.println("3. Problema de encoding");
            throw new RuntimeException("Assinatura do token inválida", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println("Erro ao validar token: " + ex.getMessage());
            throw new RuntimeException("Erro ao validar o token", ex);
        }
    }

//    public Claims validateToken(String token) {
//        try {
//            return Jwts.parser()
//                    .verifyWith(getSigningKey())
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//        } catch (ExpiredJwtException ex) {
//            throw new RuntimeException("Token expirado", ex);
//        } catch (SignatureException ex) {
//            throw new RuntimeException("Assinatura do token inválida", ex);
//        } catch (MalformedJwtException ex) {
//            throw new RuntimeException("Token malformado", ex);
//        } catch (JwtException | IllegalArgumentException ex) {
//            throw new RuntimeException("Erro ao validar o token", ex);
//        }
//    }

    public String gerarToken(User usuario) {
        // Garanta que está incluindo a role como claim
        return Jwts.builder()
            .claim("role", usuario.getRole())  // Linha crucial!
            .subject(usuario.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 dia
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
