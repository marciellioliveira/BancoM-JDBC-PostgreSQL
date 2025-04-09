package br.com.marcielli.BancoM.entity;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoCartao;
import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "idCartao")
public class CartaoCredito extends Cartao {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private float limiteCreditoPreAprovado;
	
	private float taxaUtilizacao;

	private float taxaSeguroViagem;

	private float totalGastoMesCredito;
	
	public CartaoCredito() {}
	
	public CartaoCredito(String numeroCartao, TipoConta tipoConta, CategoriaConta categoriaConta, TipoCartao tipoCartao,
			boolean status, String senha, Conta conta) {
		super(numeroCartao, tipoConta, categoriaConta, tipoCartao, status, senha, conta);
	}

	public float getLimiteCreditoPreAprovado() {
		return limiteCreditoPreAprovado;
	}

	public void setLimiteCreditoPreAprovado(float limiteCreditoPreAprovado) {
		this.limiteCreditoPreAprovado = limiteCreditoPreAprovado;
	}

	public float getTaxaUtilizacao() {
		return taxaUtilizacao;
	}

	public void setTaxaUtilizacao(float taxaUtilizacao) {
		this.taxaUtilizacao = taxaUtilizacao;
	}

	public float getTaxaSeguroViagem() {
		return taxaSeguroViagem;
	}

	public void setTaxaSeguroViagem(float taxaSeguroViagem) {
		this.taxaSeguroViagem = taxaSeguroViagem;
	}

	public float getTotalGastoMesCredito() {
		return totalGastoMesCredito;
	}

	public void setTotalGastoMesCredito(float totalGastoMesCredito) {
		this.totalGastoMesCredito = totalGastoMesCredito;
	}

}
