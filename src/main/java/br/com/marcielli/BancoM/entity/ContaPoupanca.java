package br.com.marcielli.BancoM.entity;

import java.math.BigDecimal;
import java.util.Random;


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
public class ContaPoupanca extends Conta {
	
	private static final long serialVersionUID = 1L;
	

	private BigDecimal taxaAcrescRend;
	
	private BigDecimal taxaMensal;
	
//	public ContaPoupanca(BigDecimal taxaAcrescRend, BigDecimal taxaMensal) {
//		super();
//		this.taxaAcrescRend = taxaAcrescRend;	
//		this.taxaMensal = taxaMensal;		
//	}
//	



}
