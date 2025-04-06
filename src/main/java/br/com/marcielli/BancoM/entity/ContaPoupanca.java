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
	
	public ContaPoupanca(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta, String numeroConta, float taxaAcrescRend, float taxaMensal) {
		super(cliente, tipoConta,categoriaConta, saldoConta, numeroConta);
		this.taxaAcrescRend = taxaAcrescRend;
		this.taxaMensal = taxaMensal;
	}

	public ContaPoupanca(Cliente cliente, TipoConta tipoConta, float saldoConta, String numeroConta) {
		super(cliente, tipoConta, saldoConta, numeroConta);
		
		setTaxaAcrescRend(saldoConta);
		setTaxaMensal(saldoConta);
		
		this.taxaAcrescRend = getTaxaAcrescRend();
		this.taxaMensal = getTaxaMensal();
//		
//		CategoriaConta categoriaConta = null;
//		
//		if(saldoConta <= 1000) {
//			
//			categoriaConta = CategoriaConta.COMUM;
//			super.setCategoriaConta(categoriaConta);
//			this.taxaAcrescRend = 0.005f;			
//			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//		}
//		
//		if(saldoConta > 1000 && saldoConta <= 5000) {
//			
//			categoriaConta = CategoriaConta.SUPER;
//			super.setCategoriaConta(categoriaConta);
//			this.taxaAcrescRend = 0.007f;			
//			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//		}
//		
//		if(saldoConta > 5000) {
//			
//			categoriaConta = CategoriaConta.PREMIUM;
//			super.setCategoriaConta(categoriaConta);
//			this.taxaAcrescRend = 0.009f;			
//			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
//			
//		}	
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
