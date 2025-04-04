package br.com.marcielli.BancoM.entity;

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

	private float taxaAcrescRend;
	
	private float taxaMensal;
	
	public ContaPoupanca() {}

	public ContaPoupanca(float saldo) {		
		setTaxaAcrescRend(saldo);
		setTaxaMensal(saldo);
		super.setCategoriaConta(saldo);	
	}

	public ContaPoupanca(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta,
			String numeroConta, float taxaAcrescRend, float taxaMensal) {
		super(cliente, tipoConta, categoriaConta, saldoConta, numeroConta);
		setTaxaAcrescRend(saldoConta);
		setTaxaMensal(saldoConta);
		super.setCategoriaConta(saldoConta);	
	}
	

	public float getTaxaAcrescRend() {
		return taxaAcrescRend;
	}

	public void setTaxaAcrescRend(float saldo) {
		
		if(saldo <= 1000f) {
			this.taxaAcrescRend = 0.005f;					
		}
		
		if(saldo > 1000f && saldo <= 5000f) {
			this.taxaAcrescRend = 0.007f;			
		}	
		
		if(saldo > 5000f)
		this.taxaAcrescRend = 0.009f;
	}

	public float getTaxaMensal() {	
		return taxaMensal;
	}

	public void setTaxaMensal(float saldo) {
		
		if(saldo <= 1000f) {
			
			this.taxaMensal = (float) (Math.pow(1+getTaxaAcrescRend(), 1.0/12) - 1);		
		}
		
		if(saldo > 1000f && saldo <= 5000f) {
			
			this.taxaMensal = (float) (Math.pow(1+getTaxaAcrescRend(), 1.0/12) - 1);
		}		
		
		if(saldo > 5000f)
		this.taxaMensal = (float) (Math.pow(1+getTaxaAcrescRend(), 1.0/12) - 1);		
	}
	
}
