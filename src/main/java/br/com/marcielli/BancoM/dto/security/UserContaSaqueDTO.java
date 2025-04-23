package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

public record UserContaSaqueDTO(Long idContaOrigem, BigDecimal valor) {

}
