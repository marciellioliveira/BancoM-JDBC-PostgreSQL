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

	public ContaPoupanca(Cliente cliente, TipoConta tipoConta, float saldoConta, String numeroConta) {
		super(cliente, tipoConta, saldoConta, numeroConta);
		
		CategoriaConta categoriaConta = null;
		
		if(saldoConta <= 1000f) {
			categoriaConta = CategoriaConta.COMUM;
			super.setCategoriaConta(categoriaConta);		
			this.taxaAcrescRend = 0.005f;
			
			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
		}
		
		if(saldoConta > 1000f && saldoConta <= 5000f) {
			categoriaConta = CategoriaConta.SUPER;
			super.setCategoriaConta(categoriaConta);
			this.taxaAcrescRend = 0.007f;
			
			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
		}
		
		if(saldoConta > 5000f) {
			categoriaConta = CategoriaConta.PREMIUM;
			super.setCategoriaConta(categoriaConta);
			this.taxaAcrescRend = 0.009f;
			
			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
			
		}		
	}
	
	
	
	public void atualizaCategoria(float saldoConta) {
		
	 CategoriaConta categoriaConta = null;
	 if(saldoConta <= 1000f) {
			categoriaConta = CategoriaConta.COMUM;
			super.setCategoriaConta(categoriaConta);	
		}
		
		if(saldoConta > 1000f && saldoConta <= 5000f) {
			categoriaConta = CategoriaConta.SUPER;
			super.setCategoriaConta(categoriaConta);
		}
		
		if(saldoConta > 5000f) {
			categoriaConta = CategoriaConta.PREMIUM;
			super.setCategoriaConta(categoriaConta);
		}
	}
	
	public void atualizaTaxas(float saldoConta) {
		
		if(saldoConta <= 1000f) {
			this.taxaAcrescRend = 0.005f;			
			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
		}
		
		if(saldoConta > 1000f && saldoConta <= 5000f) {
			this.taxaAcrescRend = 0.007f;			
			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);
		}
		
		if(saldoConta > 5000f) {
			this.taxaAcrescRend = 0.009f;			
			this.taxaMensal = (float) (Math.pow(1+taxaAcrescRend, 1.0/12) - 1);			
		}		
		
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
		
		if(saldo > 5000f) {
			this.taxaAcrescRend = 0.009f;
		}
		
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
		
		if(saldo > 5000f) {
			
			this.taxaMensal = (float) (Math.pow(1+getTaxaAcrescRend(), 1.0/12) - 1);
		}
				
	}

	@Override
	public String toString() {
		return "ContaPoupanca [taxaAcrescRend=" + taxaAcrescRend + ", taxaMensal=" + taxaMensal + "]";
	}

	
	

	

	
	
}
