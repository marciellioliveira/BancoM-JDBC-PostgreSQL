package br.com.marcielli.BancoM.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
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
public class ContaCreateSaqueDTO {

	@NotNull(message = "O id do cliente origem deve ser informado.")
	private Long idClienteOrigem;
	
	//@NotNull(message = "O id da conta origem deve ser informado.")
	private Long idContaOrigem;
	
	@NotNull(message = "O valor do saque deve ser informado.")
	private BigDecimal valor;
}
