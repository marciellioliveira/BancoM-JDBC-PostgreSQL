package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;

import br.com.marcielli.bancom.enuns.TipoCartao;

public record UserCartaoPagCartaoDTO(Long idUsuario, Long idCartao, TipoCartao tipoCartao, BigDecimal valor, String senha) {

}
