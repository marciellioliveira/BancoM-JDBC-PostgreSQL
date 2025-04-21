package br.com.marcielli.BancoM.service;

import java.time.Instant;
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
import br.com.marcielli.BancoM.filter.JwtAuthenticationFilter;
import br.com.marcielli.BancoM.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtService {
	
	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

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
	
	public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public Long extractClienteId(String token) {
        return extractAllClaims(token).get("clienteId", Long.class);
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
		return extractExpiration(token).before(Date.from(Instant.now())); // Verifica se o token expirou com a hora UTC
		//return extractExpiration(token).before(new Date());
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
			 System.out.println("CHAVE SECRETA (validação): " + secretKey);
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
		System.out.println("CHAVE SECRETA (geração): " + secretKey);
	    Map<String, Object> claims = new HashMap<>();
	    claims.put("userId", user.getId());
	    
	    // Verificando se o usuário é um cliente (ROLE=USER)
//	    if (user.getCliente() != null) {
//	        claims.put("clienteId", user.getCliente().getId());
//	    } else {
//	        // Para administradores, o clienteId não é necessário
//	        claims.put("clienteId", null); // ou você pode usar -1L se preferir
//	    }
	    
	    claims.put("authorities",
	            user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

	    Instant issuedAt = Instant.now();
	    Instant expiration = issuedAt.plusMillis(accessTokenExpire);

	    // Log para verificar os tempos de emissão e expiração
	    System.out.println("Issued At: " + issuedAt);
	    System.out.println("Expiration: " + expiration);

	    return Jwts.builder()
	               .setClaims(claims)
	               .setSubject(user.getUsername())
	               .setIssuedAt(Date.from(issuedAt))
	               .setExpiration(Date.from(expiration))
	               .signWith(getSigninKey())
	               .compact();
//		System.out.println("CHAVE SECRETA (geração): " + secretKey);
//		Map<String, Object> claims = new HashMap<>();
//	    claims.put("userId", user.getId());    
//	    
//	    
//	    if (user.getCliente() != null) {
//	        claims.put("clienteId", user.getCliente().getId());
//	    }
//	    
//	    claims.put("authorities",
//	            user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
//
//	    Instant issuedAt = Instant.now();
//	    Instant expiration = issuedAt.plusMillis(accessTokenExpire);
//
//	    // Log para verificar os tempos de emissão e expiração
//	    System.out.println("Issued At: " + issuedAt);
//	    System.out.println("Expiration: " + expiration);
//
//	    return Jwts.builder()
//	               .setClaims(claims)
//	               .setSubject(user.getUsername())
//	               .setIssuedAt(Date.from(issuedAt))
//	               .setExpiration(Date.from(expiration))
//	               .signWith(getSigninKey())
//	               .compact();


	}
	
	public String generateAccessToken(User user, Map<String, Object> claims) {
	    Instant issuedAt = Instant.now();
	    Instant expiration = issuedAt.plusMillis(accessTokenExpire);

	    System.out.println("Issued At: " + issuedAt);
	    System.out.println("Expiration: " + expiration);

	    return Jwts.builder()
	            .setClaims(claims)
	            .setSubject(user.getUsername())
	            .setIssuedAt(Date.from(issuedAt))
	            .setExpiration(Date.from(expiration))
	            .signWith(getSigninKey())
	            .compact();
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
