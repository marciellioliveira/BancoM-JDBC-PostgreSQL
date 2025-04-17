package br.com.marcielli.BancoM.dto;

import java.math.BigDecimal;

import br.com.marcielli.BancoM.enuns.TipoSeguro;

public class SeguroResponseDTO {
	
	private Long id;
    private TipoSeguro tipo;
    private BigDecimal valorMensal;
    private BigDecimal valorApolice;
    private Boolean ativo;
    private Long idCartao;

}
