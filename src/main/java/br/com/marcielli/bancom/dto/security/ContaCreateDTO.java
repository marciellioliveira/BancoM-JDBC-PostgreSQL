package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;

import br.com.marcielli.bancom.enuns.TipoConta;

public record ContaCreateDTO(Long idUsuario, TipoConta tipoConta, BigDecimal saldoConta) {

}
