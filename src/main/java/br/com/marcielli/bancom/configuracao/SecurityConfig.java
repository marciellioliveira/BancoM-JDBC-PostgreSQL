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
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;

import br.com.marcielli.bancom.exception.AcessoNegadoException;
import br.com.marcielli.bancom.filter.JwtAuthFilter;
import br.com.marcielli.bancom.handler.CustomAccessDeniedHandler;
import br.com.marcielli.bancom.service.UserClienteService;
import br.com.marcielli.bancom.service.UserSecurityService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	@Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(); // Define o handler como um bean
    }
	
	private final JwtAuthFilter jwtAuthFilter;
	
	private final UserClienteService usuarioService;
	private final PasswordEncoder passwordEncoder;
	private final UserSecurityService userSecurityService;
	private final AccessDeniedHandler accessDeniedHandler;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserClienteService usuarioService, PasswordEncoder passwordEncoder, UserSecurityService userSecurityService, AccessDeniedHandler accessDeniedHandler) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.usuarioService = usuarioService;
		this.passwordEncoder = passwordEncoder;
		this.userSecurityService = userSecurityService;
		 this.accessDeniedHandler = accessDeniedHandler;
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
                    
                    // ADMIN pode listar todos os usuários
                    .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                    
                    // GET por ID: ADMIN e BASIC
                    .requestMatchers(HttpMethod.GET, "/users/*").hasAnyRole("ADMIN", "BASIC")
                    
                    // PUT por ID: ADMIN e BASIC
                    .requestMatchers(HttpMethod.PUT, "/users/*").hasAnyRole("ADMIN", "BASIC")
                    
                    // DELETE por ID: ADMIN e BASIC
                    .requestMatchers(HttpMethod.DELETE, "/users/*").hasAnyRole("ADMIN", "BASIC")
                    
                    //Porque está assim?
                    //ADMIN: é o dono do sistema (tipo um superuser)ele pode editar/deletar qualquer conta, MENOS a própria
                    //BASIC: é só o usuário comum que pode editar/deletar só os próprios dados.
                    //Então o resto da autenticação, a parte mais exigente vai estar no service.
                    
                .anyRequest().authenticated())       
            .exceptionHandling(exceptionHandling -> 
            exceptionHandling.accessDeniedHandler(accessDeniedHandler))
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
