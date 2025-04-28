package br.com.marcielli.BancoM.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.BancoM.enuns.TipoCartao;
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
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class CartaoCreateDTO {

	@NotNull(message = "O id do cliente deve ser informado.")
	private Long idCliente;	
	
	@NotNull(message = "O id da conta deve ser informado.")
	private Long idConta;	
	
	@NotNull(message = "O tipo de cartão (Crédito ou Débito) deve ser informado.")
	private TipoCartao tipoCartao;
	
	@NotBlank(message = "A senha deve ser informada.")
	public String senha;
	
}
