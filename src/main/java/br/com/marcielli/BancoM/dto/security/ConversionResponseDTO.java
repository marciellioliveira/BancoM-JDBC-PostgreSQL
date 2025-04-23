package br.com.marcielli.BancoM.dto.security;

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
public class ConversionResponseDTO {
	
	private BigDecimal valorOriginal;
    private String moedaOrigem;
    private String moedaDestino;
    private BigDecimal valorConvertido;
    private Double taxaCambio;


}
