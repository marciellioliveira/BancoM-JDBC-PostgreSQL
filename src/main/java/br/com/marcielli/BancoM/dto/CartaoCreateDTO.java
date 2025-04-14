package br.com.marcielli.BancoM.dto;

import br.com.marcielli.BancoM.enuns.TipoCartao;
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
public class CartaoCreateDTO {

	@NotBlank(message = "O id do cliente deve ser informado.")
	private Long idCliente;	
	
	@NotBlank(message = "O id da conta deve ser informado.")
	private Long idConta;	
	
	@NotBlank(message = "O tipo de cartão (Crédito ou Débito) deve ser informado.")
	private TipoCartao tipoCartao;
	
	@NotBlank(message = "A senha deve ser informada.")
	public String senha;
	
}
