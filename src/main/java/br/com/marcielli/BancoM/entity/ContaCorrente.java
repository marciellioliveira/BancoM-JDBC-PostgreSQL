package br.com.marcielli.BancoM.entity;

import java.math.BigDecimal;


import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@PrimaryKeyJoinColumn(name = "idConta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContaCorrente extends Conta {
	
	private static final long serialVersionUID = 1L;	

	private BigDecimal taxaManutencaoMensal;
	
//	public ContaCorrente(BigDecimal taxaManutencaoMensal) {
//		super();
//		this.taxaManutencaoMensal = taxaManutencaoMensal;		
//	}

	

}
