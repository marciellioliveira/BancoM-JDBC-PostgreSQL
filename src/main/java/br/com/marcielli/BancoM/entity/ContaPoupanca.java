package br.com.marcielli.BancoM.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "idConta")
public class ContaPoupanca extends Conta {
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private float taxaAcrescRend;
	
	@JsonIgnore
	private float taxaMensal;
	
	public ContaPoupanca() {}	

	public ContaPoupanca(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta, String numeroConta, List<Taxas> taxas){
		super(cliente, tipoConta,categoriaConta, saldoConta, numeroConta, taxas);		
		
		for(Taxas taxasPoupanca : taxas) {
			if(taxasPoupanca.getTipoConta() == TipoConta.POUPANCA) {
				this.taxaAcrescRend = taxasPoupanca.getTaxaAcrescRend();
				this.taxaMensal = taxasPoupanca.getTaxaMensal();
			}
		}		
	}

	public float getTaxaAcrescRend() {
		return taxaAcrescRend;
	}
	
	public void setTaxaAcrescRend(float taxaAcrescRend) {
		this.taxaAcrescRend = taxaAcrescRend;
	}

	public float getTaxaMensal() {
		return taxaMensal;
	}

	public void setTaxaMensal(float taxaMensal) {
		this.taxaMensal = taxaMensal;
	}

	@Override
	public String toString() {
		return "ContaPoupanca [taxaAcrescRend=" + taxaAcrescRend + ", taxaMensal=" + taxaMensal + "]";
	}
}
