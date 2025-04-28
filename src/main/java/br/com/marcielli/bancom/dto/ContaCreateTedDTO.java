package br.com.marcielli.bancom.dto;

import java.math.BigDecimal;

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
public class ContaCreateTedDTO {
	
	
	private Long idClienteOrigem;
	private Long idContaOrigem;
	private BigDecimal valor;
	private Long idClienteDestino;

}
