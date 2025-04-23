package br.com.marcielli.BancoM.dto;


import jakarta.validation.constraints.NotNull;
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
public class CartaoPagarFaturaDTO {

	@NotNull(message = "O id do cliente deve ser informado.")
	private Long idClienteOrigem;	
	
	@NotNull(message = "O id da conta deve ser informado.")
	private Long idContaOrigem;	

	//Pagar com saldo da conta
	//ID cartão como Request Param	
	
}
