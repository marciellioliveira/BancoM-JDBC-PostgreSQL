package br.com.marcielli.bancom.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.bancom.enuns.TipoCartao;
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

	private Long idCliente;	
	private Long idConta;	
	private TipoCartao tipoCartao;
	private String senha;
	
}
