package br.com.marcielli.BancoM.dto;

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
public class CartaoConsultarFaturaResponseDTO {
	
	
	private Long idCliente;	
	private Long idConta;		
	
	//ID cartão como Request Param	

}
