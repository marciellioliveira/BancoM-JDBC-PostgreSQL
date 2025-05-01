package br.com.marcielli.bancom.configuracao;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public DataSource dataSource() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/bd_newbank");
		dataSource.setUsername("postgres");
		dataSource.setPassword("admin");
		return dataSource;
	}

	@Bean
	public JdbcUserDetailsManager users(DataSource dataSource, PasswordEncoder encoder) {

		UserDetails admin = User.builder().username("admin").password(encoder.encode("adminsupersecretpass"))
				.roles("ADMIN").build();

		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

		if (jdbcUserDetailsManager.userExists(admin.getUsername())) {
			// Atualiza o usuário existente (incluindo a senha codificada)
			jdbcUserDetailsManager.updateUser(admin);
			System.err.println("Usuário admin atualizado com sucesso!");
		} else {
			// Cria novo usuário
			jdbcUserDetailsManager.createUser(admin);
			System.err.println("Usuário admin criado com sucesso!");
		}
		return jdbcUserDetailsManager;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		return http

				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth.requestMatchers("/", "/home", "/auth/**", "/login/**").permitAll()
						.requestMatchers("/favicon.ico", "/error").permitAll() // Adicione esta linha
						.requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/webjars/**").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/whitelist.txt").permitAll()
						.requestMatchers("/user").hasRole("USER")
						.requestMatchers("/admin").hasRole("ADMIN")
						.anyRequest().authenticated())
				.httpBasic(basic -> basic.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				
				

				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

				.build();

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
