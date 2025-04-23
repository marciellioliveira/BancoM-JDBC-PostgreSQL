package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

import br.com.marcielli.BancoM.enuns.TipoCartao;

public record UserCartaoPagCartaoDTO(Long idCartao, TipoCartao tipoCartao, BigDecimal valor, String senha) {

}
