package br.com.marcielli.BancoM.dto;

import java.math.BigDecimal;

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
public class ContaCreateTransferenciaDTO {
	
	@NotBlank(message = "O id do cliente origem deve ser informado.")
	private Long idClienteOrigem;
	
	@NotBlank(message = "O id da conta origem deve ser informado.")
	private Long idContaOrigem;
	
	@NotBlank(message = "O valor da transferência deve ser informado.")
	private BigDecimal valor;
	
	@NotBlank(message = "O id do cliente destino deve ser informado.")
	private Long idClienteDestino;

}
