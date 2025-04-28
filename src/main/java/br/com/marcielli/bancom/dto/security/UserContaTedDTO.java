package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;

public record UserContaTedDTO(Long idUsuario, Long idContaOrigem, BigDecimal valor) {

}
