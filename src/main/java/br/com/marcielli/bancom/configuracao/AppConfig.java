package br.com.marcielli.bancom.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.marcielli.bancom.utils.GerarNumeros;

@Configuration
public class AppConfig {

	@Bean
    public GerarNumeros gerarNumeros() {
        return new GerarNumeros();
    }
}
