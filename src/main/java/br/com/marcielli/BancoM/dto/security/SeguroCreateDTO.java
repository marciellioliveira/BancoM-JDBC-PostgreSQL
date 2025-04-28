package br.com.marcielli.BancoM.dto.security;

import br.com.marcielli.BancoM.enuns.TipoSeguro;

public record SeguroCreateDTO(Long idUsuario, Long idCartao, TipoSeguro tipoSeguro) {
	
}
