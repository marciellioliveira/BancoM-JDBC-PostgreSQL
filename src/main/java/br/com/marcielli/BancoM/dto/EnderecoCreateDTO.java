package br.com.marcielli.BancoM.dto;

import jakarta.validation.constraints.Min;
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
public class EnderecoCreateDTO {
	
	@Min(value = 8, message = "O cep deve ter 8 digitos sem pontos e traços.")
	@NotBlank(message = "O cep deve ser informado")
	private String cep;	
	
	@NotBlank(message = "A cidade deve ser informada")
	private String cidade;	
	
	@NotBlank(message = "O estado deve ser informado")
	private String estado;	
	
	@NotBlank(message = "A rua deve ser informada")
	private String rua;	
	
	@NotBlank(message = "O número deve ser informado")
	private String numero;	
	
	@NotBlank(message = "O bairro deve ser informado")
	private String bairro;	
	
	@NotBlank(message = "O complemento deve ser informado")
	private String complemento;

}
