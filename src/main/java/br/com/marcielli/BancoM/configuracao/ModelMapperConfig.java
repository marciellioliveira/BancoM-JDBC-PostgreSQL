package br.com.marcielli.BancoM.configuracao;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class ModelMapperConfig { //Essa classe vai produzir um Bean com o método que vamos implementar.
	//Ela fará a ligação entre ClienteCreateDTO e ClienteResponseDTO
	
	@Bean
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ModelMapper modelMapper() {
		return new ModelMapper(); //Classe que utilizamos para fazer a transferência.
	}

}
