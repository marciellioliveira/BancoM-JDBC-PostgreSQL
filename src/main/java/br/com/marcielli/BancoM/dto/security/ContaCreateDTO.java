package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

import br.com.marcielli.BancoM.enuns.TipoConta;

public record ContaCreateDTO(TipoConta tipoConta, BigDecimal saldoConta) {

}
