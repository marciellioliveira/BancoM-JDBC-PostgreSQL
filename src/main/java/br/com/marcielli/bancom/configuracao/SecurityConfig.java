package br.com.marcielli.bancom.configuracao;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.marcielli.bancom.filter.JwtAuthFilter;
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
	
//	@Bean
//    public AccessDeniedHandler accessDeniedHandler() {
//        return new CustomAccessDeniedHandler(); // Define o handler como um bean
//    }
	
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
            		.requestMatchers(
            		        "/",
            		        "/home",
            		        "/auth/login", // Libera explicitamente o endpoint de login
            		        "/login",      // Libera o endpoint alternativo
            		        "/favicon.ico",
            		        "/error",
            		        "/css/**",
            		        "/js/**",
            		        "/images/**",
            		        "/static/**",
            		        "/webjars/**",
            		        "/users"  
            		    ).permitAll()
            			
            		
                    
                    //Porque está assim?
                    //ADMIN: é o dono do sistema (tipo um superuser)ele pode editar/deletar qualquer conta, MENOS a própria
                    //BASIC: é só o usuário comum que pode editar/deletar só os próprios dados.
                    //Então o resto da autenticação, a parte mais exigente vai estar no service.
                    
                .anyRequest().authenticated())                   	
//            .exceptionHandling(exceptionHandling -> 
//            exceptionHandling.accessDeniedHandler(accessDeniedHandler))
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .build();
    }
//	
//	@Bean
//	public CorsFilter corsFilter() {
//	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	    CorsConfiguration config = new CorsConfiguration();
//	    
//	   // config.setAllowedOriginPatterns(List.of("*"));
//	    
//	    // Permite todos métodos (GET, POST, etc)
//	    config.setAllowedMethods(List.of("*"));
//	    
//	    // Permite todos headers
//	    config.setAllowedHeaders(List.of("*"));
//	    
//	    // Permite headers de autenticação
//	    config.setExposedHeaders(List.of("Authorization"));
//	    
//	    // Permite credenciais
//	    config.setAllowCredentials(true);
//	    
//	    source.registerCorsConfiguration("/**", config);
//	    return new CorsFilter(source);
//	}
	
	
	 
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
