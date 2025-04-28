package br.com.marcielli.bancom.dto;

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
public class CartaoUpdateDTO {

	@NotNull(message = "O id do cliente deve ser informado.")
	private Long idCliente;	
	
	@NotNull(message = "O id da conta deve ser informado.")
	private Long idConta;		
	
	@NotBlank(message = "A senha deve ser informada.")
	public String senha;
	
}
