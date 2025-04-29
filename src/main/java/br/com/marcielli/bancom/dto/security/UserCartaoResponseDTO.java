package br.com.marcielli.bancom.dto.security;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
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
public class UserCartaoResponseDTO {

	private Long id;
	private Long idConta;
	private TipoCartao tipoCartao;
	private String numeroCartao;
	private Boolean status;
	public String senha;
	private TipoConta tipoConta;
	private CategoriaConta categoriaConta;
	private BigDecimal limiteCreditoPreAprovado;
	private BigDecimal limiteDiarioTransacao;
}
