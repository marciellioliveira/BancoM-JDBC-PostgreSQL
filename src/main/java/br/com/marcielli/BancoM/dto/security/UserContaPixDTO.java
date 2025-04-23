package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

public record UserContaPixDTO(Long idContaOrigem, BigDecimal valor) {

}
