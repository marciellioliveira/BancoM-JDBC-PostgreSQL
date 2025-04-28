package br.com.marcielli.bancom.dto;

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
public class ContaResponseDTO {
	
	private Long idCliente;		
	private TipoConta tipoConta;	
	private float saldoConta;	

}
