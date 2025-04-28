package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoConta;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class UserContaResponseDTO {

	private Long id;
	
	@Enumerated(EnumType.STRING)
	private TipoConta tipoConta;
	
	@Enumerated(EnumType.STRING)
	private CategoriaConta categoriaConta;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private BigDecimal taxaManutencaoMensal;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private BigDecimal taxaAcrescRend;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private BigDecimal taxaMensal;		
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private BigDecimal saldoConta;	
	
	private String numeroConta;	
	
	private String pixAleatorio;
	
	private Boolean status;
}
