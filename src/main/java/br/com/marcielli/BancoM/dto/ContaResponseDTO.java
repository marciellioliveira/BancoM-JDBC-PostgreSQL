package br.com.marcielli.BancoM.dto;

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
public class ContaResponseDTO {
	
	private Long idCliente;		
	private TipoConta tipoConta;	
	private float saldoConta;	

}
