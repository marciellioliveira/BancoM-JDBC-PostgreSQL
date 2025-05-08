package br.com.marcielli.bancom.configuracao;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.marcielli.bancom.filter.JwtAuthFilter;
import br.com.marcielli.bancom.handler.CustomAccessDeniedHandler;
import br.com.marcielli.bancom.service.UserClienteService;
import br.com.marcielli.bancom.service.UserSecurityService;

import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	@Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(); // Define o handler como um bean
    }
	
	private final JwtAuthFilter jwtAuthFilter;
	
	private final AccessDeniedHandler accessDeniedHandler;
	private final AuthenticationEntryPoint authenticationEntryPoint;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, PasswordEncoder passwordEncoder, 
			AccessDeniedHandler accessDeniedHandler, AuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtAuthFilter = jwtAuthFilter;
        this.accessDeniedHandler = accessDeniedHandler;
	    this.authenticationEntryPoint = authenticationEntryPoint;
	}
	
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers(
            		        "/",
            		        "/home",
            		        "/auth/users", // Libera explicitamente o endpoint de cadastro
            		        "/auth/login", // Libera explicitamente o endpoint de login
            		        "/login",      // Libera o endpoint alternativo
            		        "/favicon.ico",
            		        "/error",
            		        "/css/**",
            		        "/js/**",
            		        "/images/**",
            		        "/static/**",
            		        "/webjars/**" 
            		    ).permitAll()
            		 // Todas as outras rotas exigem autenticação
                .anyRequest().authenticated()
                )                   	
            .exceptionHandling(exceptionHandling -> 
            exceptionHandling
            	.accessDeniedHandler(accessDeniedHandler) // Tratamento de erros de acesso negado 403
            	.authenticationEntryPoint(authenticationEntryPoint) // Para 401
            	)
            .httpBasic(basic -> basic.disable()) 
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .build();
    }
	//Porque está assim?
    //ADMIN: é o dono do sistema (tipo um superuser)ele pode editar/deletar qualquer conta, MENOS a própria
    //BASIC: é só o usuário comum que pode editar/deletar só os próprios dados.
    //Então o resto da autenticação, a parte mais exigente vai estar no service.

	 
	@Bean // ele é tipo o chefe
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		System.out.println("Criando AuthenticationManager");
		return config.getAuthenticationManager(); // retorna o manager que vai processar login/autenticação
	} // ele vai validar se o usuário existe e se a senha tá certa, já faz isso
		// automático, pq já é configurado pelo próprio String

	@Bean
	public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
	    return new DefaultMethodSecurityExpressionHandler();
	}

}
