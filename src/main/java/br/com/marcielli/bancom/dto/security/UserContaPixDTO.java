package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;

public record UserContaPixDTO(Long idUsuario, Long idContaOrigem, BigDecimal valor) {

}
