package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;
import java.util.List;


import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.TaxaManutencao;
import br.com.marcielli.BancoM.entity.Transferencia;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
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
