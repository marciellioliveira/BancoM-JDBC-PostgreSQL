package br.com.marcielli.bancom.dto.security;

import com.fasterxml.jackson.annotation.JsonInclude;

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
public class UserClienteResponseDTO {
	
	private Long id;
	private String nome;
	private Long cpf;	
	private String cep;	
	private String cidade;	
	private String estado;	
	private String rua;	
	private String numero;	
	private String bairro;	
	private String complemento;
	private boolean clienteAtivo;
}
