package br.com.marcielli.BancoM.configuracao;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.core.convert.converter.Converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Pode colocar nas controllers a annotarion @PreAuthorize("") para indicar a
						// role necessaria para autorizar o acesso ao metodo
public class SecurityConfig { // Passo 1

	// Gerar a chave pública e privada do Token JWT (generate rsa public and private
	// key: https://cryptotools.net/rsagen)
	// Tutorial para gerar a chave dentro do Windows
	// (https://cryptotools.net/rsagen)
	// Dentro da pasta
	// C:\Users\Marcielli\eclipse-workspace\BancoM\src\main\resources gerei com o
	// comando no gitbash: openssl genrsa > app.key
	// Agora para gerar a chave publica (openssl rsa -in private.pem -pubout -out
	// public.pem) só consegue gerar ela a partir da privada
	// openssl rsa -in app.key -pubout -out app.pub onde app.key é o nome da chave
	// privada e app.pub da publica (salvas no mesmo diretorio)

	// Agora, configurando o projeto para usar as duas chaves e fazer o
	// encode/decode ou seja a criptografia do JWT
	@Value("${jwt.public.key}") // Injetando valores na propriedade (que apontam para application.properties)
	private RSAPublicKey publicKey;

	@Value("${jwt.private.key}") // Injetando valores na propriedade (que apontam para application.properties)
	private RSAPrivateKey privateKey;

	// Configuração Spring Security para Token JWT
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf
	            .ignoringRequestMatchers("/login", "/users", "/h2-console/**")
	        )
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/h2-console/**").permitAll()
	            .requestMatchers(HttpMethod.POST, "/login", "/users").permitAll()
	            .anyRequest().authenticated()
	        )
	        .headers(headers -> headers
	            .frameOptions(frame -> frame.disable()) // Para H2
	        )
	        .oauth2ResourceServer(oauth2 -> oauth2
	            .jwt(jwt -> jwt
	                .jwtAuthenticationConverter(jwtAuthenticationConverter())
	            )
	        )
	        .sessionManagement(session -> session
	            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        );
	    
	    return http.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
	    JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
	    converter.setAuthorityPrefix(""); 
	    converter.setAuthoritiesClaimName("scope"); 
	    
	    JwtGrantedAuthoritiesConverter fallbackConverter = new JwtGrantedAuthoritiesConverter();
	    fallbackConverter.setAuthorityPrefix("");
	    fallbackConverter.setAuthoritiesClaimName("roles");

	    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
	    jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
	        Collection<GrantedAuthority> authorities = converter.convert(jwt);
	        if (authorities == null || authorities.isEmpty()) {
	            authorities = fallbackConverter.convert(jwt);
	        }
	       
	        System.out.println("Authorities reconhecidas: " + authorities);
	        return authorities;
	    });
	    
	    return jwtConverter;
	}

	// Configurando o Encoder e Decoder do JWT
	@Bean // Descriptografa
	public JwtDecoder jwtDecoder() {
		// Vamos usar uma dependência da apache numbus-jose-jwt para criar um decoder a
		// partir da chave pública
		return NimbusJwtDecoder.withPublicKey(publicKey).build();
	}

	@Bean // Encriotografa
	public JwtEncoder jwtEncoder() {
		// Como se fosse a chave do JWT para depois fazer o encode
		JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build();
		var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwks);
	}

	@Bean // Bean de Segurança para criptografar as senhas com o algoritmo do Bcrypt
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	

}