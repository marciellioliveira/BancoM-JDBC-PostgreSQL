package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApoliceResponseDTO(
        String numeroApolice,
        LocalDate dataContratacao,
        String numeroCartao,
        String nomeTitular,
        BigDecimal valorApolice,
        String condicoesAcionamento,
        boolean ativo
) {}