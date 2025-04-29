package br.com.marcielli.bancom.entity;

import java.io.Serializable;
import java.util.List;

import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Cartao implements Serializable {

	private Long id;

	@JsonIgnore
	private TipoConta tipoConta;

	@JsonIgnore
	private CategoriaConta categoriaConta;

	private TipoCartao tipoCartao;
	
	private String numeroCartao;
	
	private boolean status;
	
	public String senha;

	@JsonBackReference
	private Conta conta;

	@JsonBackReference
	private Fatura fatura;

	@JsonBackReference
	private List<Seguro> seguros;
	
	
}
