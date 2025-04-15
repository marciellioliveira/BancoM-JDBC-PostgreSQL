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
public class CartaoUpdateLimiteDTO {

	@NotBlank(message = "O id do cliente deve ser informado.")
	private Long idCliente;	
	
	@NotBlank(message = "O id da conta deve ser informado.")
	private Long idConta;		
	
	@NotBlank(message = "O valor do novo limite deve ser informado.")
	private BigDecimal novoLimite;	
}
