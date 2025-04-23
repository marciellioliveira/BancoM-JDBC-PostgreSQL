package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

public record UserContaDepositoDTO(Long idContaOrigem, BigDecimal valor) {

}
