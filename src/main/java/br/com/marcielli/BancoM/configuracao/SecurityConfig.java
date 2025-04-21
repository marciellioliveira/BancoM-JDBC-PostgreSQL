package br.com.marcielli.BancoM.configuracao;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.marcielli.BancoM.filter.JwtAuthenticationFilter;
import br.com.marcielli.BancoM.service.UserDetailsServiceImp;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Lazy
	private final UserDetailsServiceImp userDetailsServiceImp;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomLogoutHandler logoutHandler;
    
    public SecurityConfig(UserDetailsServiceImp userDetailsServiceImp, JwtAuthenticationFilter jwtAuthenticationFilter, CustomLogoutHandler logoutHandler) {
    	  this.userDetailsServiceImp = userDetailsServiceImp;
          this.jwtAuthenticationFilter = jwtAuthenticationFilter;
          this.logoutHandler = logoutHandler;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    	AuthenticationManagerBuilder authenticationManagerBuilder = 
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsServiceImp)
                                     .passwordEncoder(passwordEncoder()); // Configura o PasswordEncoder
        return authenticationManagerBuilder.build();
    }
    
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	
    	return http
    			.cors(Customizer.withDefaults()) // habilita CORS para o Front end
    	        .csrf(AbstractHttpConfigurer::disable)
    	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    	        .authorizeHttpRequests(req -> req    	       
    	        		
    	        	//Permitindo para o Front end
    	        	.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    	        	.anyRequest().permitAll()
    	        )
    	        .userDetailsService(userDetailsServiceImp)
    	        
    	        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
    	        .exceptionHandling(e -> e
    	            .accessDeniedHandler((request, response, accessDeniedException) -> response.setStatus(403))
    	            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
    	        )
    	        .logout(l -> l
    	            .logoutUrl("/logout")
    	            .addLogoutHandler(logoutHandler)
    	            .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
    	        )
    	        .headers(headers -> headers.disable())
    	        .build();
    }
    
}
