package br.com.marcielli.bancom.configuracao;

import br.com.marcielli.bancom.filter.JwtTokenFilter;
import br.com.marcielli.bancom.service.RedisTokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public JwtTokenFilter jwtTokenFilter(RedisTokenBlacklistService tokenBlacklistService) {
        return new JwtTokenFilter(tokenBlacklistService);
    }
}
