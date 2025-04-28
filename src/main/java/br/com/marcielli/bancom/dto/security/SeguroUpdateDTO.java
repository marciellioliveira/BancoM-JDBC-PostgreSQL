package br.com.marcielli.bancom.dto.security;

import br.com.marcielli.bancom.enuns.TipoSeguro;

public record SeguroUpdateDTO(Long idUsuario, TipoSeguro tipo) {

}
