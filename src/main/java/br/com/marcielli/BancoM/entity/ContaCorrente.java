package br.com.marcielli.BancoM.entity;


import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoConta;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "idConta")
public class ContaCorrente extends Conta {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private float taxaManutencaoMensal;
	
	public ContaCorrente() {}
	
	public ContaCorrente(float saldo) {		
		setTaxaManutencaoMensal(saldo);	
	}

	public ContaCorrente(Cliente cliente, TipoConta tipoConta, float saldoConta, String numeroConta) {
		super(cliente, tipoConta, saldoConta, numeroConta);
		
		 CategoriaConta categoriaConta = null;
		 setTaxaManutencaoMensal(saldoConta);
		 
		 this.taxaManutencaoMensal = getTaxaManutencaoMensal();
		 
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
	
	public void atualizaCategoria(float saldoConta) {
		
	 CategoriaConta categoriaConta = null;
	 setTaxaManutencaoMensal(saldoConta);
	 
	 this.taxaManutencaoMensal = getTaxaManutencaoMensal();
	 
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
	
	public float getTaxaManutencaoMensal() {		
		return taxaManutencaoMensal;
	}

	public void setTaxaManutencaoMensal(float saldo) {		
		
		if(saldo <= 1000f) {
			this.taxaManutencaoMensal = 12.00f;		
		}
		
		if(saldo > 1000f && saldo <= 5000f) {
			this.taxaManutencaoMensal = 8.00f;	
		}
		
		if(saldo > 5000f) {
			this.taxaManutencaoMensal = 0f;	
		}
	}

	@Override
	public String toString() {
		return "ContaCorrente [taxaManutencaoMensal=" + taxaManutencaoMensal + "]";
	}


	
	


	

}
