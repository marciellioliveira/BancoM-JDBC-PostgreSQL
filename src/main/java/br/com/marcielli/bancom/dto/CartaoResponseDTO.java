package br.com.marcielli.bancom.dto;

import java.math.BigDecimal;

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
public class CartaoResponseDTO {
	
	private Long idUsuario;
	private Long idConta;
	private TipoCartao tipoCartao;
	private String numeroCartao;
    private Boolean status;
    private BigDecimal limiteCredito;
	private String senha;
	
//	private Long idCliente;	
//	private Long idConta;	
//	private TipoCartao tipoCartao;
//	public String senha;

}
