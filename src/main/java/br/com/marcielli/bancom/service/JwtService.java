package br.com.marcielli.bancom.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import br.com.marcielli.bancom.entity.User;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

	private final String ChaveSecreta = Base64.getEncoder().encodeToString("mInh4Ch4v3sECrEt4@#1234567890".getBytes());

	private Key getChaveAssinatura() {
	    
	    byte[] chaveBytes = ChaveSecreta.getBytes(StandardCharsets.UTF_8);
	    return Keys.hmacShaKeyFor(chaveBytes);
	}

	public String gerarToken(User usuario) {

		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("role", usuario.getRole()); // role do usuário no token

		return Jwts.builder().setClaims(claims).setSubject(usuario.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis())) // segundos minutos horas
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 horas pra expirar
																							
				.signWith(SignatureAlgorithm.HS256, getChaveAssinatura()).compact();
	}

	// aqui eu to usando o T como um coringa, por que posso querer extrair o email,
	// ou a data de expiração
	public <T> T extrairClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extrairTodosOsClaims(token);
		return claimsResolver.apply(claims);
	} // ele pega o token, e devolve o que eu pedi (o email ou qualquer outra coisa)

	public String extrairUsername(String token) {
		return extrairClaim(token, Claims::getSubject);
	}

	private Claims extrairTodosOsClaims(String token) {
	    return Jwts.parser()
	               .setSigningKey(getChaveAssinatura())
	               .parseClaimsJws(token)
	               .getBody();
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
