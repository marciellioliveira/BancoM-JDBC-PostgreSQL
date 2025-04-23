package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

import br.com.marcielli.BancoM.entity.Conta;

public record UserContaTedDTO(Long idContaOrigem, BigDecimal valor) {

}
