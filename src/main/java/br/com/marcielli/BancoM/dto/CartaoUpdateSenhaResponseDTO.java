package br.com.marcielli.BancoM.dto;


import java.io.Serializable;

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
public class CartaoUpdateSenhaResponseDTO  implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	private Long idCliente;	
	private Long idConta;	
	private String novaSenha;	

}
