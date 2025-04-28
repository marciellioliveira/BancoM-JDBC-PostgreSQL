package br.com.marcielli.bancom.dto;

import java.math.BigDecimal;

import br.com.marcielli.bancom.enuns.TipoSeguro;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SeguroResponseDTO {
	
	private Long id;
    private TipoSeguro tipo;
    private BigDecimal valorMensal;
    private BigDecimal valorApolice;
    private Boolean ativo;
    private Long idCartao;

}
