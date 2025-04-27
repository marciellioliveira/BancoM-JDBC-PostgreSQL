package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

public record UserContaPixDTO(Long idUsuario, Long idContaOrigem, BigDecimal valor) {

}
