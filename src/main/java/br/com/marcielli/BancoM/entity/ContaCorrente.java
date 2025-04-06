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
	
	public ContaCorrente(Cliente cliente, TipoConta tipoConta, CategoriaConta categoriaConta, float saldoConta, String numeroConta, float taxaManutencaoMensal) {
		super(cliente, tipoConta, categoriaConta, saldoConta, numeroConta);
		this.taxaManutencaoMensal = taxaManutencaoMensal;
	}

	public ContaCorrente(Cliente cliente, TipoConta tipoConta, float saldoConta, String numeroConta) {
		super(cliente, tipoConta, saldoConta, numeroConta);
		
		
		
//		 CategoriaConta categoriaConta = null;
//		 setTaxaManutencaoMensal(saldoConta);
//		 
//		 this.taxaManutencaoMensal = getTaxaManutencaoMensal();
//		 
//		 if(saldoConta <= 1000f) {
//			 this.taxaManutencaoMensal = 12.00f;
//				categoriaConta = CategoriaConta.COMUM;
//				super.setCategoriaConta(categoriaConta);	
//			}
//			
//			if(saldoConta > 1000f && saldoConta <= 5000f) {
//				this.taxaManutencaoMensal = 8.00f;	
//				categoriaConta = CategoriaConta.SUPER;
//				super.setCategoriaConta(categoriaConta);
//			}
//			
//			if(saldoConta > 5000f) {
//				this.taxaManutencaoMensal = 0f;	
//				categoriaConta = CategoriaConta.PREMIUM;
//				super.setCategoriaConta(categoriaConta);
//			}
	}
	
	

	public float getTaxaManutencaoMensal() {
		return taxaManutencaoMensal;
	}

	public void setTaxaManutencaoMensal(float taxaManutencaoMensal) {
		this.taxaManutencaoMensal = taxaManutencaoMensal;
	}

	@Override
	public String toString() {
		return "ContaCorrente [taxaManutencaoMensal=" + taxaManutencaoMensal + "]";
	}


	
	


	

}
