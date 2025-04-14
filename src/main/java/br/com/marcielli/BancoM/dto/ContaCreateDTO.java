package br.com.marcielli.BancoM.dto;

import java.math.BigDecimal;

import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.validation.constraints.NotBlank;
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
public class ContaCreateDTO {
	
	@NotBlank(message = "O id do cliente deve ser informado.")
	private Long idCliente;	
	
	@NotBlank(message = "O tipo de conta (Corrente ou Poupança) deve ser informado. ")
	private TipoConta tipoConta;
	
	@NotBlank(message = "Você deve digitar um valor inicial para abertura da conta.")
	private BigDecimal saldoConta;	

}
