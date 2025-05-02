package br.com.marcielli.bancom.configuracao;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider; 
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;

import br.com.marcielli.bancom.filter.JwtAuthFilter;
import br.com.marcielli.bancom.service.UserClienteService;
import br.com.marcielli.bancom.service.UserSecurityService;





@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	private final JwtAuthFilter jwtAuthFilter;
	
	private final UserClienteService usuarioService;
	private final PasswordEncoder passwordEncoder;
	private final UserSecurityService userSecurityService;
	

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserClienteService usuarioService, PasswordEncoder passwordEncoder, UserSecurityService userSecurityService) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.usuarioService = usuarioService;
		this.passwordEncoder = passwordEncoder;
		this.userSecurityService = userSecurityService;
	}


	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            		// Permite acesso livre às páginas de login, home, etc.
                    .requestMatchers("/", "/home", "/auth/**", "/login/**").permitAll()
                    .requestMatchers("/favicon.ico", "/error").permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/webjars/**").permitAll()
            		
                    // Permite o cadastro de usuários (POST)
                    .requestMatchers(HttpMethod.POST, "/users").permitAll()
                    
                    // Admin pode acessar e modificar todos os dados
                    .requestMatchers("/users").hasRole("ADMIN")   // Para listar usuários
                    
                    // Admin pode acessar e modificar qualquer usuário específico
                    .requestMatchers("/users/{id}").hasRole("ADMIN")  // Para acessar um usuário específico
                    .requestMatchers(HttpMethod.PUT, "/users/{id}").hasRole("ADMIN") // Para editar um usuário específico
                    .requestMatchers(HttpMethod.DELETE, "/users/{id}").hasRole("ADMIN") // Para deletar um usuário específico      
                    
                    
                    
                    //Acesso basic - Cliente
                    .requestMatchers(HttpMethod.GET, "/users/{id}").hasRole("BASIC")
                    
                .anyRequest().authenticated())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .build();
    }
	
	@Bean // ele é tipo o chefe
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager(); // retorna o manager que vai processar login/autenticação
	} // ele vai validar se o usuário existe e se a senha tá certa, já faz isso
		// automático, pq já é configurado pelo próprio String

	@Bean
	public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
	    return new DefaultMethodSecurityExpressionHandler();
	}

}
