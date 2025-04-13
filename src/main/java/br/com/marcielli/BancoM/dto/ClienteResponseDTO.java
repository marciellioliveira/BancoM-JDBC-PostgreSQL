package br.com.marcielli.BancoM.dto;

import br.com.marcielli.BancoM.entity.Endereco;
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
public class ClienteResponseDTO {
	
	private String nome;		
	private Long cpf;	
	private Endereco endereco;

}
