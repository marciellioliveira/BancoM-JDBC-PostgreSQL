package br.com.marcielli.bancom.entity;

import java.math.BigDecimal;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContaPoupanca extends Conta {

	private BigDecimal taxaAcrescRend;
	private BigDecimal taxaMensal;
}
