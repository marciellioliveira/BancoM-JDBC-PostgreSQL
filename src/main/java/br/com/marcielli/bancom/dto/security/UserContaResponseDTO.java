package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;

import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;
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
public class UserContaResponseDTO {

	private Long id;
	private TipoConta tipoConta;
	private CategoriaConta categoriaConta;
	private BigDecimal taxaManutencaoMensal;
	private BigDecimal taxaAcrescRend;
	private BigDecimal taxaMensal;
	private BigDecimal saldoConta;
	private String numeroConta;
	private String pixAleatorio;
	private Boolean status;
}
