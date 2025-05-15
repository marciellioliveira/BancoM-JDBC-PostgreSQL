package br.com.marcielli.bancom.dto.security;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.marcielli.bancom.enuns.TipoSeguro;

public record SeguroUpdateDTO(
		
		Long idUsuario, 
		@JsonProperty("tipoSeguro")
		TipoSeguro tipo
		
) {}
