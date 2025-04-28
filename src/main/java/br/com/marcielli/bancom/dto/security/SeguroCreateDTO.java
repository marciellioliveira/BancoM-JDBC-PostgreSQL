package br.com.marcielli.bancom.dto.security;

import br.com.marcielli.bancom.enuns.TipoSeguro;

public record SeguroCreateDTO(Long idUsuario, Long idCartao, TipoSeguro tipoSeguro) {
	
}
