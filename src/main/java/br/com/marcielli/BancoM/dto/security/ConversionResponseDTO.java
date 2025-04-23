package br.com.marcielli.BancoM.dto.security;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;

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
@JsonFormat(shape = JsonFormat.Shape.STRING)
public class ConversionResponseDTO {
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private BigDecimal valorOriginal;
	
    private String moedaOrigem;
    
    private String moedaDestino;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal valorConvertido;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal taxaCambio;


}
