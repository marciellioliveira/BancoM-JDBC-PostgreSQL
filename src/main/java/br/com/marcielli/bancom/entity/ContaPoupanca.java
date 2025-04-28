package br.com.marcielli.bancom.entity;

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
public class ContaPoupanca extends Conta {
	
	private static final long serialVersionUID = 1L;
	

	private BigDecimal taxaAcrescRend;
	
	private BigDecimal taxaMensal;

	
}
