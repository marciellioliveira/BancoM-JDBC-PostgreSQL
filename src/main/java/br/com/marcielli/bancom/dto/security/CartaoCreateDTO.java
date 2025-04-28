package br.com.marcielli.bancom.dto.security;

import br.com.marcielli.bancom.enuns.TipoCartao;

public record CartaoCreateDTO(Long idUsuario, Long idConta, TipoCartao tipoCartao, String senha) {

}
