package br.com.marcielli.BancoM.dto;

import br.com.marcielli.BancoM.enuns.TipoCartao;
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
public class CartaoResponseDTO {
	
	private Long idCliente;	
	private Long idConta;	
	private TipoCartao tipoCartao;
	public String senha;

}
