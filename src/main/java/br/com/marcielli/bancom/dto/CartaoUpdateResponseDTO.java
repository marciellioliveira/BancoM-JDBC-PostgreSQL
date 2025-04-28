package br.com.marcielli.bancom.dto;

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
public class CartaoUpdateResponseDTO {
	
	private Long idCliente;	
	private Long idConta;	
	public String senha;
	
}
