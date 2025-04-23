package br.com.marcielli.BancoM.configuracao;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class JacksonConfig {

	  @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }
}
