package br.com.marcielli.BancoM.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${application.security.jwt.secret-key}")
	private String secretKey;

	@Value("${application.security.jwt.access-token-expiration}")
	private long accessTokenExpire;

	@Value("${application.security.jwt.refresh-token-expiration}")
	private long refreshTokenExpire;

	private final TokenRepository tokenRepository;

	public JwtService(TokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isValid(String token, UserDetails user) {
		String username = extractUsername(token);

		boolean validToken = tokenRepository.findByAccessToken(token).map(t -> !t.isLoggedOut()).orElse(false);

		return (username.equals(user.getUsername())) && !isTokenExpired(token) && validToken;
	}

	public boolean isValidRefreshToken(String token, User user) {
		String username = extractUsername(token);

		boolean validRefreshToken = tokenRepository.findByRefreshToken(token).map(t -> !t.isLoggedOut()).orElse(false);

		return (username.equals(user.getUsername())) && !isTokenExpired(token) && validRefreshToken;
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
		Claims claims = extractAllClaims(token);
		return resolver.apply(claims);
	}

	public Claims extractAllClaims(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(getSigninKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (JwtException e) {
			throw new RuntimeException("Token inválido ou expirado");
		}
	}

	public String generateAccessToken(User user) {

		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("authorities",
				user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

		return Jwts.builder().setClaims(claims).setSubject(user.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + accessTokenExpire)).signWith(getSigninKey()).compact();

	}

	public String generateRefreshToken(User user) {
		return generateToken(user, refreshTokenExpire);
	}

	private String generateToken(User user, long expireTime) {
		return Jwts.builder().setSubject(user.getUsername()) // Alterado para setSubject
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expireTime)).signWith(getSigninKey()) 
				.compact();
	}

	private SecretKey getSigninKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

}
