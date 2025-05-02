package br.com.marcielli.bancom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.marcielli.bancom.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;


@Service
public class JwtService {

	@Value("${jwt.secret}")
    private String secretKeyString;

	// Obtém a chave secreta para assinatura
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    //Validando o token e retornando as claims
    public Claims validateToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String gerarToken(User usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", usuario.getRole());

        return Jwts.builder()
            .claims(claims) // Nova API
            .subject(usuario.getUsername()) // Nova API
            .issuedAt(new Date(System.currentTimeMillis())) // Nova API
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Nova API
            .signWith(getSigningKey()) // Usar SecretKey diretamente
            .compact();
    }

    public <T> T extrairClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extrairTodosOsClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extrairUsername(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    private Claims extrairTodosOsClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Date extrairExpiration(String token) {
        return extrairClaim(token, Claims::getExpiration);
    }

    public boolean tokenExpirado(String token) {
        return extrairExpiration(token).before(new Date());
    }

    public boolean tokenValido(String token, String username) {
        final String usernameEncontrado = extrairUsername(token);
        return (usernameEncontrado.equals(username) && !tokenExpirado(token));
    }
}
